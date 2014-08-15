;;
;; Copyright Christophe Roeder, August 2014


(ns clojure-craft.craft-pos)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])
(use 'clojure.xml)
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
(defn parse-tokens 
"...returns a vector of Tokens"
[sentence sentence-number filename]
  (loop [tokens (rest sentence)
         token-number 1
         token (first sentence)
         spans []]
    (let [token-record (Token. token-number
                         (:cat (:attrs token)) 
                         (first (:content token))
                         0 0) 
          my-spans (conj spans token-record)]
      (cond (not (empty? tokens))
          (recur 
           (rest tokens) (inc token-number) (first tokens) 
           my-spans)
          :t  
          my-spans ))))

(defn parse-tokens-new
"...returns a vector of Tokens"
[sentence sentence-number filename]
  (map (fn [token-xml]
       (Token. -1; token-number
               (:cat (:attrs token-xml)) 
               (first (:content token-xml))
               0 0) )))      
      


(defn load-from-xml
"Load pos from xml only. Returns a vector of vectors of Tokens."
;; ... {  ... :content {:tag :sentence, :attrs nil, :content [{:tag :tok, :attrs {:cat "NN"}, :content ["Abstract"]}]} }
[pos-filename text-filename]
	(let [in-data (:content (first (read-craft-file pos-filename)))]
               (loop [sentence (first in-data)
                      data (rest in-data)
                      sentence-list []
                      sentence-number 1]
                 (let [new-sentence
                       (parse-tokens (:content sentence) sentence-number text-filename)
                       my-sentence-list (conj sentence-list new-sentence) ]
                   (cond (not (empty? data)) 
                       (recur (first data) (rest data) 
                              my-sentence-list
                              (inc sentence-number) )
                       :t
                       my-sentence-list) ))))


;; not doing the last token. Can'[t do the work in the recur call, it will lose the case where there's just one thing and possibly the last as well.
(defn add-token-spans-sentence
"Input: a list of token-records from a sentence, the article text, and the offset of the sentence start
Description: adds span information for each token discovered in the article text starting at sentence-offset.
Returns: (updated list of token-records packaged in a sentence-record)"
[sentence-tokens text sentence-offset sentence-number]
  (loop [tokens        (rest sentence-tokens)
         token         (first sentence-tokens)
         index         sentence-offset 
         token-start   0
         token-end     0
         new-tokens    []]
            (let [token-start   (.indexOf text (:text token) index)   
                  token-end     (+ token-start (.length (:text token)))
                  new-token  (cond (> token-start -1)
                                   (Token. (:token-number token) (:pos token) (:text token) token-start token-end)
                                   (<= token-start -1)
                                   (Token. (:token-number token) (:pos token)  (:text token) 0 0)     )
                  local-new-tokens (conj new-tokens new-token)]
              (cond (not (empty? tokens))
                    (recur (rest tokens) (first tokens) token-end token-start token-end   local-new-tokens)
                    :t 
                    (Sentence. "foo.txt" sentence-number nil sentence-offset token-end  local-new-tokens)))))

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
(println "add-token-spans: " sentence-number sentence-offset)
      (cond (not (empty? sentences-lists))
            (recur (first sentences-lists) (rest sentences-lists) 
                   (conj new-sentences sentence)
                   (:end sentence) ; sentence-offset = (:end sentence)
                   (inc sentence-number))
          :t (conj new-sentences sentence)))))

(defn test-sentence [token-list sentence-number article-text]
  (cond (> (count token-list) 0)
  (loop [tokens (rest token-list)
         token (first token-list)]

    (let [extracted-token-text (.substring article-text (:start token) (:end token))
          token-text (:text token)]
      (cond (.equals token-text extracted-token-text)
            (println "good " sentence-number (:token-number token) token-text)
            :t  
            (println "bad " sentence-number (:token-number token) (str "\"" extracted-token-text "\"") (str "\"" token-text "\"") (:start token) (:end token))))

    (cond (not (empty? tokens))
          (recur (rest tokens) (first tokens))
          :t nil))
  :t  nil))

(defn test-article-spans [output article-text]
  (loop [sentence (first output)
         sentences (rest output)
         temp-sentence-number 0]
    (test-sentence (:tokens sentence) temp-sentence-number article-text)
    (cond (not (empty? sentences))
          (recur (first sentences) (rest sentences) (inc temp-sentence-number) )
          :t nil)))
 
(defn print-sentence [tokens sentence-number]
  (map (fn [token-number token] 
         (println sentence-number "-->" token-number token)) 
       (iterate inc 0) tokens))

(defn print-sentences [output article-text]
  (map (fn [sentence-number sentence] 
         (print-sentence sentence sentence-number)) 
       (iterate inc 0) output))
;; map f coll val
;; a sneaky use of map to get a sequence of index values in there.
;; the common/beginner way to think of map is that you write
;; a function that takes two arguments combining the first into
;; the second somehow. For example if the arguments are the next
;; item in the list and a sum, + can be used to add in another number.
;; Another example would be the next item in the list and another list,
;; where conj would be the way to combine and item and the list.
;; Here there are two collections, the function is passed a value from each.
;; Though the first collection is infinite, processing stops when the smaller
;; collection runs out.

(defn test-run []
  (test-article-spans 
   (add-token-spans
    (load-from-xml sample-pos-file sample-text-file)
    (slurp sample-text-file))
   (slurp sample-text-file)))

(defn print-run []
;   (add-token-spans
  (print-sentences
    (load-from-xml sample-pos-file sample-text-file)
    (slurp sample-text-file)))
