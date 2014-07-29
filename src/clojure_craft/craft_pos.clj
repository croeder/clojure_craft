(ns clojure-craft.craft-pos)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])
(use 'clojure.xml)
; parse the txt files and generate word spans
(def base "/home/croeder/git/craft/craft-1.0/genia-xml/pos")
(def sample-article "11532192.txt.xml")
(def text-file (str base "/" sample-article))

;; FSCK! we need a list of all the tokens and their  spans to use as keys into
;; each of the onotology data and dep. parse.
;; - use the plain text and this file (genia pos) to reconstruct.

(defrecord Token [file start end pos text] )

(defn read-craft-file [file]
	(xml-seq (parse (java.io.File. file))))

(defn find-spans [])

;---> {:tag :tok, :attrs {:cat JJ}, :content [specific]}
(defn parse-tokens [sentence]
  (loop [tokens sentence
         token (first sentence)
         spans []]
    (println "--->" token)
    (cond (not (empty? tokens))
          (recur 
           (rest tokens) 
           (first tokens) 
           (conj spans (Token. 'file 
                              'start 'end 
                              (:cat (:attrs token)) 
                              (first (:content token)))))
          :t  
          spans)))

;; ... {  ... :content {:tag :sentence, :attrs nil, :content [{:tag :tok, :attrs {:cat "NN"}, :content ["Abstract"]}]} }
(defn load-from-xml [file]
	(let [in-data (:content (first (read-craft-file text-file)))]
               (loop [sentence (first in-data)
                     data (rest in-data)
                      sentence-list []]
                 (cond (not (empty? data)) 
                       (recur (first data) (rest data) (conj sentence-list (parse-tokens (:content sentence))))
                       :t
                       nil))))
 



