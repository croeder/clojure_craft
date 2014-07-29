(ns clojure-craft.craft-txt)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])
; parse the txt files and generate word spans

(def base "/home/croeder/git/craft/craft-1.0/articles/txt")
(def sample-article "11532192.txt")
(def text-file (str base "/" sample-article))


genia POS!!!

(defn process-craft-file 
"This encapsulates the open reader and works through
the values of the lazy seq calling the function f
on each line."
[file f]
  (with-open [rdr (reader file)]
    (doseq [line (line-seq rdr)]
      (println (f line)))) )

(defn tokenize-by-space 
"assumes the input text has been tokenized and re-composed with spaces
...except for punctuation, crap"
[line]
  (loop [start 0
         spans []
         token ""
         parts (split item-string #"\s+")  ]  
    (cond (isEmpty? parts) 
          spans
          :t
          (let [token-start (.indexOf line token start)
                token-end (+ start (.length token))  ;; finish here
                token-span (token-start token-end token)]
            (recur (token-end) 
                 (conj spans token-span)
                 (first parts) 
                 (rest parts))))))



