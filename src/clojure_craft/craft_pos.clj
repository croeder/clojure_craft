;;
;; Copyright Christophe Roeder, August 2014


(ns clojure-craft.craft-pos)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])
(use 'clojure.xml)
;(def pos-base "/home/croeder/git/craft/craft-1.0/genia-xml/pos")
;(def txt-base "/home/croeder/git/craft/craft-1.0/articles/txt")
;;(def sample-pos-file (str pos-base "/" "11532192.txt.xml"))
;(def sample-pos-file (str pos-base "/" "short.txt.xml"))
;(def sample-text-file (str txt-base "/" "11532192.txt"))

(defrecord Token [token-number part-of-speech text start end dependency anno-list] )
(defrecord Sentence [filename  sentence-number text start end tokens] )

(defn read-craft-file 
"returns a list of xml structures "
[file]
  (try
	(xml-seq (parse (java.io.File. file)))
        (catch Exception e (println "error reading craft xml file:" file)
               (println (.getMessage e))
               ;(.printStackTrace e) 
               (println "continuing")
               nil)))

; :TOK, :attrs {:cat JJ}, :content [specific]}
(defn- parse-tokens
"...returns a vector of Tokens"
[sentence sentence-number filename]
  (map (fn [token-xml token-number]
       (Token.  token-number
               (:cat (:attrs token-xml)) 
               (first (:content token-xml))
               0 0 
               nil nil))
       sentence
       (iterate inc 1)))
      
(defn load-from-xml
"Load pos from xml only. Returns a vector of vectors of Tokens.
returns nil on error"
;; ... {  ... :content {:tag :sentence, :attrs nil, :content [{:tag :tok, :attrs {:cat "NN"}, :content ["Abstract"]}]} }
[pos-filename text-filename]
(let [parse-results (read-craft-file pos-filename)
      in-data (cond parse-results (:content (first parse-results))
                    :t (list))]
  (map (fn  [sentence sentence-number]
         (parse-tokens (:content sentence) sentence-number text-filename))
       in-data
       (iterate inc 1))))

(defn- add-token-spans-sentence
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
                         (Token. (:token-number token) (:part-of-speech token) 
                                 (:text token) token-start token-end nil nil)
                         (<= token-start -1) ;; error
                         (Token. (:token-number token) (:part-of-speech token)  (:text token) 0 0 nil nil)     )
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
      (cond (not (empty? sentences-lists))
            (recur (first sentences-lists) (rest sentences-lists) 
                   (conj new-sentences sentence)
                   (:end sentence) ; sentence-offset = (:end sentence)
                   (inc sentence-number))
          :t (conj new-sentences sentence)))))

(defn- test-sentence [token-list sentence-number article-text]
  (map (fn [token count]
         (let [extracted-token-text (.substring article-text (:start token) (:end token))
               token-text (:text token)]
           (cond (.equals token-text extracted-token-text)
                 ;(println "good " sentence-number (:token-number token) token-text)
                 nil
                 :t  
                 ;(println "bad " sentence-number (:token-number token) 
                 ;  (str "\"" extracted-token-text "\"") (str "\"" token-text "\"") 
                 ;  (:start token) (:end token)))))
                 (list sentence-number (:token-number token) 
                       (str "\"" extracted-token-text "\"") 
                       (str "\"" token-text "\"") ))))
       token-list (iterate inc 1) )) 


(defn- test-article-spans [output article-text]
  (map (fn [sentence sentence-number]
         (test-sentence (:tokens sentence) sentence-number article-text))
       output 
       (iterate inc 1)  ))

;;;;;;;;;;;;;;;;;;;;
(defn- print-sentence [tokens sentence-number]
  (map (fn [token-number token] 
         (println sentence-number "-->" token-number token)) 
       (iterate inc 0) tokens))

;(defn- print-sentences [output article-text]
;  (map (fn [sentence-number sentence] 
;         (print-sentence sentence sentence-number)) 
;       (iterate inc 0) output))

;(defn- test-run []
;  (test-article-spans 
;   (add-token-spans
;    (load-from-xml sample-pos-file sample-text-file)
;    (slurp sample-text-file))
;   (slurp sample-text-file)))

;(defn- print-run []
;   (add-token-spans
;    (load-from-xml sample-pos-file sample-text-file)
;    (slurp sample-text-file)))

;(defn- simple-run []  
;(print-sentences    (load-from-xml sample-pos-file sample-text-file)
;    (slurp sample-text-file))
;)
;;;;;;;;;;;;;

(defn load-pos [pos-file text-file]
  (let [xml-parse     (load-from-xml pos-file text-file)]
    (cond xml-parse
          (add-token-spans xml-parse (slurp text-file))
          :t 
          nil )))
