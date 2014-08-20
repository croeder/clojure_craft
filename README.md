# clojure_craft

A Clojure library designed to explore the Colorado Richly Annotated Full-Text Corpus (CRAFT Corpus)  http://bionlp-corpora.sourceforge.net/CRAFT/

## Usage

### Tokens, and Part-of_Speech: craft_pos
    returns a vector of Sentence records that each contain a vector
    of Token records. The Tokens include span positions into the document text.
    Tokens have a number relative to the beginning of the Sentence, starting at 1.
    Sentences have numbers starting at 1.

### Dependency: craft_dep
    reads a Genia formatted dependency file
    returns a vector of vectors of Dependency records with 
    IDs the same as token number within Sentence as above for POS.

### Ontology Terms: craft_xml 
    reads an xml file of ontology annotations
    returns a map of annotation_id to a pair of ontology_id and span text
    This needs to evolve to an Annotation record with span information

### Unify: craft_unify
    uses craft_pos, craft_dep, craft_xml to read the various types of
    data and unifies them into a common data structure...
    
#### Strategy: 

POS and Dependency are by numbered sentence and token. Vectors should be
sufficient.

Iterating over the terms and finding the token to add the ontology
annotation would be aided by a span-index on the tokens. Iterating 
over the tokens and using an index to find ontology annotations
might result in too many empty lookups.

### Analysis: craft_analysis
    uses craft_unify to get simultaneous and integrated access to the
    various annotations....

## License
   Artistic 2.0

## Notes

Identifying tokens is done differently in different contexts in NLP in general, and specifically in CRAFT.  Some files have implicit token order by just listing tokens: genia-pos and dependency files.  Some files use a character span in the text: the ontology concepts. The challenge here is to unify the data from these sources, and it starts with finding a common way to identify the tokens. 

The genia-pos files list all the tokens, so I start there with a function to generate a list of lists of Token structures.  The Tokens are grouped by sentence. A second function takes this list of tokens and finds their spans in the text, updating the token structures (vice immutability) with that data. It also builds a list of Sentence structures.

This sets things up so the ontology data can be integrated based on its use of identifying annotated tokens by span.

Dependency files list tokens in order and can be added at any time.

To make this useful for others that may not be interested in this Clojure code, an output format should be defined. CRAFT is small and fixed-size, so just using the code here to deliver the annotations in a uniform format is feasible.



Copyright © 2014 Christophe Roeder


