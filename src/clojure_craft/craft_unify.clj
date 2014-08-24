;;
;; Copyright Christophe Roeder, August 2014


(ns clojure-craft.craft-unify)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])

(use '[clojure-craft.craft-pos])
(use '[clojure-craft.craft-dep])
(use '[clojure-craft.craft-xml])

;;(def home "/Users/croeder")
(def home "/home/croeder")
(def txt-base (str home "/git/craft/craft-1.0/articles/txt"))  ; <id>.txt
(def pos-base (str home "/git/craft/craft-1.0/genia-xml/pos")) ; <id>.txt.xml
(def dep-base (str home "/git/craft/craft-1.0/dependency"))    ; <id>.dep
(def ann-base (str home "/git/craft/craft-1.0/xml"))    ; /<ont>/<id>.txt.annotations.xml

(def sample-id "11532192")

(defn unify-pos-dep 
" given the names of part-of-speech and dependency data files, parses them and unifies them"
[txt-file pos-file dep-file]
  (let [sentence-seq (load-pos pos-file txt-file)   ; [Sentence] ; (:tokens sentence)
        dep-sos (load-dependencies dep-file)]  ; [ [ Dependency ] ] 
    (map (fn [sentence dep-seq] ; update each sentence
           (assoc sentence :tokens
                  (map (fn [token dep]  ; pull in a dep to each token
                         (cond (= (:token-number token) (:token-num dep))
                                           (assoc token :dependency dep)
                                           :t
                                           nil))
                       (:tokens sentence) dep-seq)))
         sentence-seq dep-sos)))

(defn unify-annotations 
""
[sentence-seq annotations-map pmid]
(map (fn [sentence] ; update each sentence
       (assoc sentence :tokens
              (map (fn [token]  ; pull in annotations to each token
                     (let [key (list pmid (:start token) (:end token))
                           ann (annotations-map key)]
                       (cond ann
                           (assoc token :anno-list 
                                  (conj (:anno-list token) ann))
                           :t 
                           token)))
                   (:tokens sentence))))
     sentence-seq))

(defn run-unify-annotations [sentence-seq id ontology]
  (println "getting annotations for " id " with " ontology)
  (let [xml-file (str ann-base "/" ontology "/" id ".txt.annotations.xml")
        annotation-map (load-annotations xml-file id)]
    (unify-annotations sentence-seq annotation-map id)))

