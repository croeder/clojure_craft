;; 
;; Copyright Christophe Roeder, August 2014
;;
;; Identifying tokens is done differently in different contexts
;; in NLP in general, and specifically in CRAFT.
;; Some files have implicit token order by just listing tokens:
;; genia-pos and dependency files.  Some files use a character 
;; span in the text: the ontology concepts. The challenge here 
;; is to unify the data from these sources, and it starts with
;; finding a common way to identify the tokens. 
;;
;; The genia-pos files list all the tokens, so I start there
;; with a function to generate a list of lists of Token structures.
;; The Tokens are grouped by sentence. A second function takes this
;; list of tokens and finds their spans in the text, updating the
;; token structures (vice immutability) with that data. It also
;; builds a list of Sentence structures.
;;
;; This sets things up so the ontology data can be integrated based
;; on its use of identifying annotated tokens by span.
;;
;; Dependency files list tokens in order and can be added at any time.
;;
;; To make this useful for others that may not be interested in this
;; Clojure code, an output format should be defined. CRAFT is small
;; and fixed-size, so just using the code here to deliver the annotations
;; in a uniform format is feasible.


;;;; tokens stand in the context of a sentence
;; to get the spans relative to the sentence we really need to know
;; the sentence breaks
;; doesn't help the @#$@@%^@# xmi xml has no line breaks
;;
;; am I stuck? finding tokens for a sentence before I can find the
;; sentence before I can complete the tokens? ....gross, but it could work

(ns clojure-craft.craft-pos)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])
(use 'clojure.xml)
; parse the txt files and generate word spans
(def pos-base "/home/croeder/git/craft/craft-1.0/genia-xml/pos")
(def txt-base "/home/croeder/git/craft/craft-1.0/articles/txt")
;;(def sample-pos-file (str pos-base "/" "11532192.txt.xml"))
(def sample-pos-file (str pos-base "/" "short.txt.xml"))
(def sample-text-file (str txt-base "/" "11532192.txt"))

(defrecord Token [token-number part-of-speech text start end] )
(defrecord Sentence [filename  sentence-number text start end tokens] )

(defn read-craft-file [file]
	(xml-seq (parse (java.io.File. file))))

; :TOK, :attrs {:cat JJ}, :content [specific]}
(defn parse-tokens [sentence sentence-number filename]
  (loop [tokens sentence
         token-number 1
         token (first sentence)
         spans []]
    (cond (not (empty? tokens))
          (recur 
           (rest tokens) (inc token-number) (first tokens) 
           (conj spans 
                 (Token. token-number
                         (:cat (:attrs token)) 
                         (first (:content token))
                         0 0) )) ; spans added later
          :t  
          spans)))

(defn load-from-xml
"Load pos from xml only. This leaves finding spans as 
a challenge, but is useful for development"
;; ... {  ... :content {:tag :sentence, :attrs nil, :content [{:tag :tok, :attrs {:cat "NN"}, :content ["Abstract"]}]} }
[pos-filename text-filename]
	(let [in-data (:content (first (read-craft-file pos-filename)))]
               (loop [sentence (first in-data)
                      data (rest in-data)
                      sentence-list []
                      sentence-number 1]
                 (cond (not (empty? data)) 
                       (recur (first data) (rest data) 
                        (conj sentence-list (parse-tokens (:content sentence) sentence-number text-filename))
                        (inc sentence-number) )
                       :t
                       sentence-list))))

(defn add-token-spans-sentence
"Input: a list of token-records from a sentence, the article text, and the offset of the sentence start
Description: adds span information for each token discovered in the article text starting at sentence-offset.
Returns: (updated list of token-records packaged in a sentence-record)"
[sentence-tokens text sentence-offset sentence-number]
  (loop [tokens        sentence-tokens 
         token         (first sentence-tokens)
         index         sentence-offset 
         token-start   0
         token-end     0
         new-tokens    []]
    (cond (not (empty? tokens))
          (do
            (let [token-start   (.indexOf text (:text token) index)   
                  token-end     (+ token-start (.length (:text token)))
                  new-token  (cond (> token-start -1)
                                   (Token. (:token-number token) (:pos token)   (:text token) token-start token-end)
                                   :t 
                                   (Token. (:token-number token) (:pos token)  (:text token) 0 0) )]
              (recur (rest tokens) (first tokens) token-end
                 token-start token-end 
                 (conj new-tokens new-token))))
          :t (Sentence. "foo.txt" sentence-number nil 0 999  new-tokens)))) 

(defn add-token-spans 
"takes article text and a list of tokens, adds span information to the tokens
returns an updated list of the tokens and a list of sentence records"
[results text]
  (loop [sentence-list (first results)
         sentences-lists (rest results)
         new-sentences []
         sentence-offset 0  
         sentence-number 0]
    (let [sentence (add-token-spans-sentence sentence-list text sentence-offset sentence-number)]
      (cond (not (empty? sentences-lists))
            (recur (first sentences-lists) (rest sentences-lists) 
                   (conj new-sentences sentence)
                   (+ sentence-offset 10)
                   (inc sentence-number))
          :t (conj new-sentences sentence)))))

(defn test-sentence [token-list sentence-number article-text]
  (cond (> (count token-list) 0)
  (loop [tokens token-list
         token (first token-list)]
    (let [extracted-token-text (.substring article-text (:start token) (:end token))
          token-text (:text token)]
      (cond (.equals token-text extracted-token-text)
            ;(println "good " sentence-number (:token-number token) token-text)
            nil
            :t  
            (println "bad " sentence-number (:token-number token) extracted-token-text  "\"" token-text "\"" (:start token) (:end token))))
    (cond (not (empty? tokens))
          (recur (rest tokens) (first tokens))
          :t nil))
  :t (do (println "emtpy sentence?") nil)))

(defn test-article-spans [output article-text]
  (loop [sentence (first output)
         sentences output
         temp-sentence-number 0]
    (test-sentence (:tokens sentence) temp-sentence-number article-text)
    (cond (not (empty? sentences))
          (recur (first sentences) (rest sentences) (inc temp-sentence-number) )
          :t nil)))

(defn test-run []
  ;;(print-token-spans (add-token-spans
  (test-article-spans 
   (add-token-spans
    (load-from-xml sample-pos-file sample-text-file)
    (slurp sample-text-file))
   (slurp sample-text-file)))

(defn test-output []
  (test-article-spans    
   (load-from-xml sample-pos-file sample-text-file) 
   (slurp sample-text-file)))
