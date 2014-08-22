# clojure_craft

A Clojure library designed to explore the Colorado Richly Annotated Full-Text Corpus (CRAFT Corpus)  http://bionlp-corpora.sourceforge.net/CRAFT/

## Quickstart

### Get clojure:

### Get emacs:

### Get emacs setup with Cider to integrate a REPL.
http://clojure-doc.org/articles/tutorials/emacs.html

Pull the project down from github by cloning. Download CRAFT from Sourceforge and put in a directory next to this project. I have
/home/croeder/git/clojure_craft/... and /home/croeder/git/craft/craft_1.0/...  The top of the craft_unify.clj file defines a
home variable so the code can find the craft files. Some of the XML files from CRAFT don't have a single top-level element
and crash as a result. I've added an arbitrary one ("foo") with good results. 

As of today (2014-08-21), I'm running the last function in clojure_unify.clj as a starting point. It loads all the files for a
single PMID and reports on the ontology terms it found in that document...to be expanded to include patterns from the 
dependency parses as well as all pmids in CRAFT.

## Usage

### Tokens, and Part-of_Speech: craft_pos
    Returns a vector of Sentence records that each contain a vector
    of Token records. The Tokens include span positions into the document text.
    Tokens have a number relative to the beginning of the Sentence, starting at 1.
    Sentences have numbers starting at 1.

### Dependency: craft_dep
    Reads a Genia formatted dependency file.
    Returns a vector of vectors of Dependency records with 
    IDs the same as token number within Sentence as above for POS.
	  (({:sentence-num 1, :token-num "1", :literal "Intraocular", :normalized "intraocular", :pos "JJ", :dep-id "2", :dep-type "NMOD"}

### Ontology Terms: craft_xml 
    Reads an xml file of ontology annotations.
    Returns a map of (file, start, end)  to ontology id:
		{("11532192.txt.annotations.xml" 14633 14643) "CHEBI:38867", 

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


