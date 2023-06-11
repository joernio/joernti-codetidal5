package io.joern.codetidal5

import better.files.File
import better.files.File.CopyOptions
import io.joern.jssrc2cpg.passes._
import io.joern.jssrc2cpg.utils.AstGenRunner
import io.joern.jssrc2cpg.{Config => JsConfig}
import io.joern.x2cpg.X2Cpg.{applyDefaultOverlays, withNewEmptyCpg}
import io.joern.x2cpg.passes.callgraph.NaiveCallLinker
import io.joern.x2cpg.passes.frontend.XTypeRecoveryConfig
import io.joern.x2cpg.utils.HashUtil
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.passes.CpgPassBase
import scopt.OptionParser

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import scala.util.{Failure, Success, Try}

/** Example program that makes use of Joern as a library
  */
object Main {

  def main(args: Array[String]): Unit = {
    println("=== JoernTI x CodeTIDAL5 ===")
    parseConfig(args.toList) match {
      case Right(config) =>
        run(config)
      case Left(errMsg) =>
        println(s"Failure: $errMsg")
        System.exit(1)
    }
  }

  private def run(config: JoernTIConfig): Unit = {
    val jsConfig       = JsConfig().withInputPath(config.inputPath).withOutputPath(config.outputPath)
    val cpgOrException = createCpg(config, jsConfig)

    cpgOrException match {
      case Success(cpg) =>
        println("[+] AST created")
        println("[+] Applying default overlays, i.e. CFG, PDG, misc.")
        applyDefaultOverlays(cpg)
        println("[+] Running post-processing passes, i.e. type recovery, call graphs, etc.")
        applyPostProcessingPasses(cpg, config)
        println("[DONE]")
      case Failure(exception) =>
        println("[FAILED]")
        println(exception)
    }
  }

  private def nanoToMinutesAndSeconds(nanoTime: Long): (Long, Long) = {
    val min = TimeUnit.MINUTES.convert(nanoTime, TimeUnit.NANOSECONDS)
    val sec =
      TimeUnit.SECONDS.convert(nanoTime - TimeUnit.NANOSECONDS.convert(min, TimeUnit.MINUTES), TimeUnit.NANOSECONDS)
    (min, sec)
  }

  def createCpg(config: JoernTIConfig, jsConfig: JsConfig): Try[Cpg] = {
    withNewEmptyCpg(config.outputPath, jsConfig) { (cpg, jsConfig) =>
      File.usingTemporaryDirectory("jssrc2cpgOut") { tmpDir =>
        val baseTimeStart                     = System.nanoTime()
        implicit val copyOptions: CopyOptions = CopyOptions(overwrite = true)
        val movedTypeDeclFiles =
          config.typeDeclarationDir.map(path =>
            File(path).list.map(f => f.copyToDirectory(File(config.inputPath))).toList
          )

        val astgenResult = new AstGenRunner(jsConfig).execute(tmpDir)
        val hash         = HashUtil.sha256(astgenResult.parsedFiles.map { case (_, file) => File(file).path })

        val astCreationPass = new AstCreationPass(cpg, astgenResult, jsConfig)
        astCreationPass.createAndApply()

        movedTypeDeclFiles.foreach(_.foreach(_.delete(swallowIOExceptions = true)))

        new TypeNodePass(astCreationPass.allUsedTypes(), cpg).createAndApply()
        new JsMetaDataPass(cpg, hash, config.inputPath).createAndApply()
        new BuiltinTypesPass(cpg).createAndApply()
        new DependenciesPass(cpg, jsConfig).createAndApply()
        new ConfigPass(cpg, jsConfig).createAndApply()
        new PrivateKeyFilePass(cpg, jsConfig).createAndApply()
        new ImportsPass(cpg).createAndApply()
        val (minutes, seconds) = nanoToMinutesAndSeconds(System.nanoTime() - baseTimeStart)
        println(s"[i] Base JS pass ran for ${minutes}m and ${seconds}s")
      }
    }
  }

  private def postProcessingPasses(cpg: Cpg, config: JoernTIConfig): List[CpgPassBase] = {
    val typeRecConfig = XTypeRecoveryConfig(enabledDummyTypes = !config.disableDummyTypes)

    List(
      new JavaScriptInheritanceNamePass(cpg),
      new ConstClosurePass(cpg),
      new JavaScriptTypeRecoveryPass(cpg, typeRecConfig)
    ) ++
      (if (!config.disableJoernTI) {
         List(
           new SliceBasedTypeInferencePass(cpg, Try(new JoernTI(spawnProcess = false)).toOption, config = config),
           new JavaScriptTypeRecoveryPass(cpg, typeRecConfig.copy(iterations = 1))
         )
       } else {
         List.empty
       }) ++
      List(new JavaScriptTypeHintCallLinker(cpg), new NaiveCallLinker(cpg))
  }

  private def applyPostProcessingPasses(cpg: Cpg, config: JoernTIConfig): Cpg = {
    val postProcessingStart = new AtomicLong(0)
    val slicingStart        = new AtomicLong(0)
    postProcessingPasses(cpg, config).foreach {
      case x: SliceBasedTypeInferencePass =>
        val start = System.nanoTime()
        x.createAndApply()
        slicingStart.addAndGet(System.nanoTime() - start)
      case x =>
        val start = System.nanoTime()
        x.createAndApply()
        postProcessingStart.addAndGet(System.nanoTime() - start)
    }

    val (postMinutes, postSeconds)   = nanoToMinutesAndSeconds(postProcessingStart.get())
    val (sliceMinutes, sliceSeconds) = nanoToMinutesAndSeconds(slicingStart.get())

    println(s"[i] Post-processing passes (excl. JoernTI slicing) ${postMinutes}m and ${postSeconds}s")
    println(s"[i] JoernTI slicing & type inference ran for ${sliceMinutes}m and ${sliceSeconds}s")
    cpg
  }

  case class JoernTIConfig(
    inputPath: String = "",
    outputPath: String = "cpg.bin",
    hostname: String = "localhost",
    port: Int = 1337,
    logTypeInference: Boolean = false,
    disableJoernTI: Boolean = false,
    disableDummyTypes: Boolean = false,
    typeDeclarationDir: Option[String] = None,
    minNumCalls: Int = 1,
    excludeOperatorCalls: Boolean = false
  )

  val optionParser: OptionParser[JoernTIConfig] = new scopt.OptionParser[JoernTIConfig]("joernti-codetidal5") {
    help("help")
    arg[String]("input")
      .required()
      .text("source code directory (JavaScript or TypeScript)")
      .action((x, c) => c.copy(inputPath = x))
    opt[String]('o', "output")
      .text("output path for the CPG (Default 'cpg.bin')")
      .action((x, c) => c.copy(outputPath = x))
    opt[String]('h', "hostname")
      .text("JoernTI server hostname (Default 'localhost')")
      .action((x, c) => c.copy(hostname = x))
    opt[Int]('p', "port")
      .text("JoernTI server port (Default 1337)")
      .action((x, c) => c.copy(port = x))
    opt[String]("typeDeclDir")
      .hidden()
      .action((x, c) => c.copy(typeDeclarationDir = Option(x)))
      .text("the TypeScript type declaration files to improve type info of the analysis")
    opt[Unit]("logTypeInference")
      .hidden()
      .action((_, c) => c.copy(logTypeInference = true))
      .text("log the slice based type inference results (Default false for performance)")
    opt[Int]('m', "min-calls")
      .text("the minimum number of calls required for a usage slice (Default 1)")
      .action((x, c) => c.copy(minNumCalls = x))
    opt[Unit]("exclude-op-calls")
      .hidden()
      .action((_, c) => c.copy(excludeOperatorCalls = true))
      .text("excludes <operator> calls from the slices, e.g. <operator>.add, <operator>.assignment, etc.")
  }

  private def parseConfig(parserArgs: List[String]): Either[String, JoernTIConfig] =
    optionParser.parse(parserArgs, JoernTIConfig()) match {
      case Some(config) => Right(config)
      case None         => Left("Could not parse command line options")
    }
}
