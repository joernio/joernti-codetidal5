import numpy as np
import pandas as pd
import evaluate
import datasets
import torch
from datasets import load_dataset
from torch.utils.data.dataloader import DataLoader
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
from tqdm import tqdm

device = torch.device("cpu")

checkpoint = "./checkpoints/checkpoint-200000"
tokenizer = AutoTokenizer.from_pretrained(checkpoint, add_prefix_space=True, model_max_length=512, use_fast=True)

model = AutoModelForSeq2SeqLM.from_pretrained(checkpoint, trust_remote_code=True)

dataset = load_dataset("kevinjesse/ManyTypes4TypeScript")
print(dataset)
id2label = {}
label2id = {}
label_list = dataset["train"].features[f"labels"].feature.names
for i, label in enumerate(label_list):
    id2label[i] = label
    label2id[label] = i

tokenized_dataset = datasets.DatasetDict.load_from_disk("./tokenized_dataset/")
print(tokenized_dataset)

total = 0
correct = 0
total_100 = 0
correct_100 = 0

split = tokenized_dataset["validation"].select(range(4096))

with tqdm(total=len(split)) as pbar:
    for sample in split:
        code = tokenizer.decode(sample['input_ids'])
        print("Code Input:\n" + code)
        exit()

        outputs = model.generate(torch.as_tensor(sample['input_ids']).unsqueeze(0), max_new_tokens=128)
        outputs = np.where(outputs != -100, outputs, tokenizer.pad_token_id)
        answer = tokenizer.decode(outputs[0])

        label = sample['labels']
        i = 0
        while i < len(label):
            if label[i] == -100:
                label[i] = tokenizer.pad_token_id
            i += 1

        decoded_labels = tokenizer.decode(label, skip_special_tokens=True)

        # print("Lables:\n" + decoded_labels)
        # print("Prediction:\n" + answer)  

        if not "No types inferred" in decoded_labels:
            type_labels = decoded_labels.split("\n")
            type_labels = [t.strip() for t in type_labels][:-1]
            pred_types = answer.split("\n")
            for i, t in enumerate(type_labels):
                if t.lower() == "any":
                    # do not count 'any' predictions
                    continue
                elif t.lower() == 'unk':
                    # count UNK as incorrect
                    total += 1
                    continue
                    
                total += 1
                if label2id[t] > 1 and label2id[t] < 102:
                    total_100 += 1
                if i < len(pred_types) and t.lower() in pred_types[i].lower():
                    correct += 1
                    if label2id[t] > 1 and label2id[t] < 102:
                        correct_100 += 1
                
                print("Type Label: " + t)
                print("Pred Label: " + (pred_types[i] if i < len(pred_types) else "none"))

        print("Overall Acc: {:.4f}".format(correct / total))
        print("Top-100 Acc: {:.4f}".format(correct_100 / total_100))

        print("______________________________")
        pbar.update(1)
