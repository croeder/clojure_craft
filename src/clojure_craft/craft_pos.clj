(ns clojure-craft.craft-pos)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])
(use 'clojure.xml)
; parse the txt files and generate word spans
(def base "/home/croeder/git/craft/craft-1.0/genia-xml/pos")
(def sample-article "11532192.txt.xml")
(def text-file (str base "/" sample-article))
;{:tag :sentence, :attrs nil, :content [{:tag :tok, :attrs {:cat "NN"}, :content ["Abstract"]}]} 
(defn read-craft-file [file]
	(xml-seq (parse (java.io.File. file))))

(defn parse-sentence [sentence]
  (loop [tokens (rest sentence)
         token (first sentence)
         spans []]
    (cond (not (empty? tokens))
          (recur 
           (rest tokens) 
           (first tokens) 
           (conj spans (list 'start 'end 'pos 'span))) ;; yeah, gonna need some more work here, okay?
          :t 
          (conj spans (list 'start 'end 'pos  'span)))
  "x")

(defn load-tokens-from-xml [file]
	(let [in-data (:content (first (read-craft-file text-file)))]
               (loop [item (first in-data)
                     data (rest in-data)
                      sentence-list []]
                 (cond (not (empty? data)) 
                       (recur (first data) (rest data) (conj sentence-list (parse-sentence item)))
                       :t
                       nil))))
 



