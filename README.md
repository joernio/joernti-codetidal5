JoernTI x CodeTIDAL5
=============================================

A standalone artifact showcasing an integration of [Joern](https://github.com/joernio/joern) and 
[JoernTI](https://github.com/joernio/type-inference-models). This adds a neural type inference pass during the usual
post-processing passes for the `jssrc2cpg` frontend.

For this process to make use of the neural type inference server, the JoernTI server must be up, and hosted with 
the corresponding `-h,--hostname` and `-p,--port`.

```
sbt stage astGenDlTask
./joernti-codetidal5 <target_source_directory> -Dlog4j.configurationFile=log4j2.xml
```

While the default values are usually all that is necessary, there are additional configurations available:

```
  input                    source code directory (JavaScript or TypeScript)
  -o, --output <value>     output path for the CPG (Default 'cpg.bin')
  -h, --hostname <value>   JoernTI server hostname (Default 'localhost')
  -p, --port <value>       JoernTI server port (Default 1337)
  -m, --min-calls <value>  the minimum number of calls required for a usage slice (Default 1)
```