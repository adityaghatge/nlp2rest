# NLP2REST's Rule Extractor

Welcome to the NLP2REST's Rule Extractor, a tool designed to extract REST API rules from natural language descriptions. This document provides a step-by-step guide on how to set up and use the tool.

## Quick deployment with Docker

If you are only interested in the Rule Extractor web service, you can quickly deploy it with Docker.

First, download the following pretrained models in the current directory:
- [model](https://drive.google.com/file/d/1-jawBqo3c3eMRkXF8Y73oLEFNSOphbpF/view?usp=share_link)
- [model_ngram](https://drive.google.com/file/d/1j1XA1dufDgqSkIGlQn97-WeKElaL8708/view?usp=share_link)

```
wget --no-check-certificate 'https://drive.google.com/uc?export=download&id=1-jawBqo3c3eMRkXF8Y73oLEFNSOphbpF' -O rest_model
wget --no-check-certificate 'https://docs.google.com/uc?export=download&confirm=any_non_empty_string_here&id=1j1XA1dufDgqSkIGlQn97-WeKElaL8708' -O rest_model.wv.vectors_ngrams.npy
```

Then, build and run the docker image with:
```
docker build -t rex .
docker run -p 4000:4000 rex
```

After few minutes, the Rule Extractor web service will start and will listen on port `4000`.


## Install requirements

> The requirements and instructions in the following sections are not required if you are using the Docker container.

To install the project dependencies, navigate to the root directory and run the following commands in your terminal:

```
pip install -r requirements.txt
python -m nltk.downloader stopwords
python -m nltk.downloader wordnet
```

This project has been thoroughly tested and validated with Python 3.9. For optimal compatibility, we highly recommend using this version. Additionally, we suggest using a virtual environment to ensure smooth operation and avoid potential conflicts among dependencies.

## Model training

To train the model, run the following command in your terminal:

```
python3 train.py
```

Please note that training the model can take a considerable amount of time, approximately 20 hours, depending on your hardware.

As an alternative, you can utilize the pre-trained models provided:

- [model](https://drive.google.com/file/d/1-jawBqo3c3eMRkXF8Y73oLEFNSOphbpF/view?usp=share_link)
- [model_ngram](https://drive.google.com/file/d/1j1XA1dufDgqSkIGlQn97-WeKElaL8708/view?usp=share_link)

Download these models and place them in this directory for use.

## Web service

To run the API, execute the `app.py` script using the following command:

```
python app.py
```

By default, the application will start on `0.0.0.0` (accessible from any IP address) and port `4000`. You can access the application via `http://localhost:4000`.

### Example Usage

Here is an example of how to use the /extract_rules endpoint with curl:

```
curl -X POST -H "Content-Type: application/json" -d '{"param_names": ["param1", "param2"], "param_name": "param1", "description": "The maximum value is 5."}' http://localhost:4000/extract_rules
```

This command sends a POST request to the `/extract_rules` endpoint with the provided JSON data, and the server will respond with the extracted rules.

### Endpoints

There are two endpoints available:

1. `/status`: This is a GET endpoint that checks if the service is running. It returns a 200 OK status code if the RuleExtractor instance is ready, and a 500 status code if the RuleExtractor failed to initialize.

2. `/extract_rules`: This is a POST endpoint that accepts JSON data in the request body and extracts rules based on the provided parameters. The JSON data should contain:
   - param_names: This is a list of parameter names in the operation. It is optional and defaults to an empty list.
   - param_name: This is a single parameter name that you want to extract rules. It is optional and defaults to an empty string.
   - description: This is a string that provides a description that you want to extract rules. It is optional and defaults to an empty string.

This command sends a POST request to the /extract_rules endpoint with the provided JSON data, and the server will respond with the extracted rules.

### Command Line Interface (CLI)

To run the CLI, execute the `nlp2rest.py` script with the required arguments:

```
python main.py --option [values]
```

The script accepts the following options:

`--specs_dir`: The path to the directory containing the specification files. This is required if `--train` is given.
`--output_model_file`: The path to the file where the trained model will be saved. This is required if `--train` is given.
`--train`: If given, the model will be trained.
`--extract_rules`: If given, rules will be extracted.
`--spec_path`: The path to the OpenAPI specification. This is required if `--extract_rules` is given.
`--settings`: The path to the settings file. This is required if `--extract_rules` is given.
`--model_name`: The name of the model. This is required if `--extract_rules` is given.

For example, to train a model, you might run:

```
python nlp2rest.py --train --specs_dir ./APIs-guru/specifications --output_model_file ./rest_model
```

And to extract rules, you might run:

```
python nlp2rest.py --extract_rules --spec_path ../specifications/swagger/fdic.yaml --settings ./settings2.yaml --model_name rest_model
```

The extracted rules are saved to a file named `found_rules.json` in the project root directory.
