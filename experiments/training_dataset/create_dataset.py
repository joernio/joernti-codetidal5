import numpy as np
import pandas as pd
import evaluate
import datasets
from datasets import load_dataset
from transformers import AutoTokenizer



def tokenize_samples(examples):
    window_size = 500
    inputs_ = {'input_ids': [], 'labels': []}
    identifiers = []
    tagged_tokens = []

    for tokens, labels in zip(examples['tokens'], examples['labels']):
        if not any(labels):
            tagged_tokens.append(tokens)
            identifiers.append([])
            continue

        l = [i for i,v in enumerate(labels) if v != None]
        curr_identifiers = []
        offset = 0
        for i, x in enumerate(l):
            curr_id = '<extra_id_{}>'.format(i % 100)
            tokens = tokens[:x + offset + 1] + [':', curr_id] + tokens[x + offset + 1:]
            curr_identifiers.append(id2label[labels[x]])
            offset += 2

        tagged_tokens.append(tokens)
        identifiers.append(curr_identifiers)

    tokenized_inputs = tokenizer(tagged_tokens, is_split_into_words=True, truncation=False, add_special_tokens=False)

    for encoding, label, ids in zip(tokenized_inputs.encodings, examples['labels'], identifiers):
        # avoid searching for the same thing twice
        last_identifier_seen = 0

        for i in range(0, len(encoding.ids), window_size):
            curr_tokens = encoding.ids[i:i + window_size]

            # create label
            if not any(label):
                label_cleaned = no_types_label
            else:
                tagged_label = []
                curr_last = last_identifier_seen
                last_index = 0
                for j, curr_label in enumerate(ids[last_identifier_seen:]):
                    curr_idx = (j + last_identifier_seen) % 100

                    found_next = False

                    for i, c in enumerate(curr_tokens[last_index:]):
                        if c == extra_tokens[curr_idx][0]:
                            tagged_label += ['<extra_id_{}>'.format(curr_idx), curr_label, '\n']
                            curr_last = j + last_identifier_seen + 1
                            last_index = last_index + i
                            found_next = True
                            break
                    
                    if not found_next:
                        break

                        
                last_identifier_seen = curr_last

                if len(tagged_label) == 0:
                    label_cleaned = no_types_label
                else:
                    tokenized_label = tokenizer(tagged_label, is_split_into_words=True, max_length=128, padding="max_length", truncation=True).input_ids
                    # mask PAD tokens
                    label_cleaned = [label if label != 0 else -100 for label in tokenized_label]

            # add CLS & EOS
            inputs_['input_ids'].append([1] + preamble + curr_tokens + [2])
            inputs_['labels'].append(label_cleaned)

    return inputs_


model_checkpoint = "Salesforce/codet5p-770m"
tokenizer = AutoTokenizer.from_pretrained(model_checkpoint, add_prefix_space=True, model_max_length=512, use_fast=True)

extra_tokens = [tokenizer(['<extra_id_{}>'.format(i)], is_split_into_words=True, truncation=False, add_special_tokens=False).input_ids for i in range(100)]
no_types_label = tokenizer(["No", "types", "inferred", "."], is_split_into_words=True, max_length=128, padding="max_length", truncation=True).input_ids
no_types_label = [label if label != 0 else -100 for label in no_types_label]
preamble = tokenizer(["Infer", "types", "for", "Javascript", ":"], is_split_into_words=True, truncation=False, add_special_tokens=False).input_ids

dataset = load_dataset("kevinjesse/ManyTypes4TypeScript")
id2label = {}
label2id = {}
label_list = dataset["train"].features[f"labels"].feature.names
for i, label in enumerate(label_list):
    id2label[i] = label
    label2id[label] = i

tokenized_dataset = dataset.map(tokenize_samples, batched=True, remove_columns=['id', 'tokens', 'labels'])

print(tokenized_dataset)
tokenized_dataset.save_to_disk("./tokenized_dataset/")