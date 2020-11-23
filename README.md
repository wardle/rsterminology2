This repository has now been archived. It has driven a SNOMED autocomplete engine in a live electronic health record system since 2009, but it has been replaced
by a more sophisticated and more complete terminology server. This is available at [https://github.com/wardle/hermes](https://github.com/wardle/hermes).

-----

# SNOMED CT fast free text search and autocompletion microservice.


This is a fast free text search and autocompletion service.

It imports a SNOMED CT release in protobuf format and builds an index. 


#### Import SNOMED-CT RF2

You import and build an index by piping in the SNOMED-CT RF2 files in protobuf format from the [go-terminology toolchain](https://github.com/wardle/go-terminology). 

```
java -jar target/termsearch-0.0.1-SNAPSHOT.jar --index 2018-06-snomed-idx --import
```

You can omit this step if I have provided a ready-built index for your use.

#### Run a REST server

```
java -jar target/termsearch-0.0.1-SNAPSHOT.jar --index 2018-06-snomed-idx --server
```

#### Using the REST server

##### Search for a concept within a specified refset

Here we pass a search term (s) "white" and search within refset 999002391000000105 which provides results from the UK 2011 census categories for ethnicity.

```
GET /snomedct/search?s=white&fsn=false&inactive=false&refset=999002391000000105
```

Here is the result:

```
[
  {
    "term": "White: Irish - England and Wales ethnic category 2011 census",
    "conceptId": 9766711000001107,
    "preferredTerm": "White: Irish - England and Wales ethnic category 2011 census"
  },
  {
    "term": "White: any other White background - England and Wales ethnic category 2011 census",
    "conceptId": 976711000000103,
    "preferredTerm": "White: any other White background - England and Wales ethnic category 2011 census"
  },
  {
    "term": "White: Gypsy or Irish Traveller - England and Wales ethnic category 2011 census",
    "conceptId": 976671000000104,
    "preferredTerm": "White: Gypsy or Irish Traveller - England and Wales ethnic category 2011 census"
  },
  {
    "term": "Mixed multiple ethnic groups: White and Asian - England and Wales ethnic category 2011 census",
    "conceptId": 976751000000104,
    "preferredTerm": "Mixed multiple ethnic groups: White and Asian - England and Wales ethnic category 2011 census"
  },
  {
    "term": "White: English or Welsh or Scottish or Northern Irish or British - England and Wales ethnic category 2011 census",
    "conceptId": 976631000000101,
    "preferredTerm": "White: English or Welsh or Scottish or Northern Irish or British - England and Wales ethnic category 2011 census"
  },
  {
    "term": "Mixed multiple ethnic groups: White and Black African - England and Wales ethnic category 2011 census",
    "conceptId": 976731000000106,
    "preferredTerm": "Mixed multiple ethnic groups: White and Black African - England and Wales ethnic category 2011 census"
  }
]
```
I recommend not returning fully specified names (fsn=false) or inactive terms (inactive=false) for most general use and in fact, you can safely leave this out as they are false by default.


##### Search via subsumption (transclosure tables) 

Here we search for "neuroendocrine" with a root parameter of 64572001, which corresponds to diagnoses essentially. We ask to limit results to a top five.

```
GET /snomedct/search?s=ben%20neuroend%20pancreas%20tumour&root=64572001&maxHits=5
```

Here is the result:

```
[
  {
    "term": "Benign neuroendocrine tumour of pancreas",
    "conceptId": 682821000119102,
    "preferredTerm": "Benign neuroendocrine neoplasm of pancreas"
  }
]
```

A single response, mapping correctly to the right diagnostic term.

##### Search from multiple roots

Here we want to create a textfield to allow entry of a substance in an allergy field. 

Note the multiple root parameters, 420881009, 373873005, 473011001 and 105590001. This means the service searches all concepts within the hierarchies defined by those roots.

```
GET /snomedct/search?s=latex&root=420881009&root=373873005&root=473011001&root=105590001&maxHits=6
```
And our result:

````
[
  {
    "term": "Latex",
    "conceptId": 111088007,
    "preferredTerm": "Latex"
  },
  {
    "term": "Latex allergy",
    "conceptId": 300916003,
    "preferredTerm": "Latex allergy"
  },
  {
    "term": "Latex protein",
    "conceptId": 255774009,
    "preferredTerm": "Latex protein"
  },
  {
    "term": "Latex specific extract",
    "conceptId": 411862007,
    "preferredTerm": "Latex specific extract"
  },
  {
    "term": "Anaphylaxis due to latex",
    "conceptId": 441593005,
    "preferredTerm": "Anaphylaxis due to latex"
  },
  {
    "term": "Anaphylaxis caused by latex",
    "conceptId": 441593005,
    "preferredTerm": "Anaphylaxis due to latex"
  }
]
```

