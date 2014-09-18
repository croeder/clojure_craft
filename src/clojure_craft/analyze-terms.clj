;;
;; Copyright Christophe Roeder, August 2014

;; This namespace contains functions analyzing or counting 
;; the terms, their POS, depdency types and frequency without
;; regard to the dependency patterns.

(ns clojure-craft.analyze-terms)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])

(use '[clojure-craft.craft-pos])
(use '[clojure-craft.craft-xml])
(use '[clojure-craft.craft-dep])
(use '[clojure-craft.craft-unify])

(defn run-unify-pos-dep [id]
  (let [txtfile (str txt-base "/" id ".txt")
        posfile (str pos-base "/" id ".txt.xml")
        depfile (str dep-base "/" id ".dep")]
  (unify-pos-dep txtfile posfile depfile)))

(defn- annotated-sentence? [sentence]
  (reduce (fn [collector token]
            (or collector (:anno-list token)))
          false (:tokens sentence)))

(defn- create-triples-from-sentence
"take a list of Token records and build a SOS of pos, dep and ontology is"
[sentence]
  (reduce (fn [collector token]
            (cond (:anno-list token)
                  (conj collector 
                        (list 
                         (:pos (:dependency token))
                         (:dep-type (:dependency token))
                         (:anno-list token)))
                  :t  collector))
          [] (:tokens sentence)))

(defn- get-type-pos-dep 
"for the given document id, for each ontology, build a sequence 
of Sentence records containing sequenes of Token records (data),
then cull Records down to triples"
[sample-id]
  (let [base (run-unify-pos-dep sample-id) 
        ;; reduce here? TODO
        data (loop [ontology (first data-dirs)
               remaining-ontologies (rest data-dirs)
               sentences base]
          (cond (not (empty? remaining-ontologies))
                (recur (first remaining-ontologies) (rest remaining-ontologies)
                       (run-unify-annotations sentences sample-id ontology))
                :t
                sentences)) ]
    (map (fn [sentence] 
           (cond (annotated-sentence? sentence)  ;; TODO, run without this
                 (create-triples-from-sentence sentence)
                 :t 
                 nil))
         data)) )

;;;;;;;;;;;;;;;;;;;;;;;;;


(defn create-ontology-pos-dep-triples
"distributes triple creation over a list of files"
[sample-id]
  (reduce (fn [collector item]
            (cond (> (count item) 0)  ;;; small to-do: can this be eliminated with nil punning?
                  (reduce conj collector item)
                  :t
                  collector))
         [] (get-type-pos-dep sample-id)
         ))

(def id-list [
11532192  15207008  15876356  16362077  17022820
11597317  15314655  15917436  16433929  17069463
11897010  15921521  16462940  17078885
12079497  15320950  15938754  16504143  17083276
12546709  15328533  16098226  16504174  
12585968  15345036  16103912  16507151  17244351
12925238  15492776  16109169  16539743  17425782
14609438  15550985  16110338  16579849  17447844
15588329  16121255  16628246  17590087
14723793  15630473  16121256  16670015  17608565
14737183  15676071  16216087  16700629  17696610
15005800  16221973  16870721
15040800  15819996  16255782  17002498
15061865  15836427  16279840  17020410
;15314659 ;17194222 ;14611657 ;15760270  
])

(defn triples-from-files
"" []
  (reduce (fn [collector id] 
            (println "looking to do: " id)
            (create-ontology-pos-dep-triples id))
          {} id-list) )

(defn run-basic "" [] 
  (doseq [x (triples-from-files)]
    (println x)))



;(NN NMOD (PR:000007164 Entrez Gene sequence))

(defn ontology-id-frequency "" []
  (reduce (fn [collector item ] 
            (let [ont-id (first (nth item 2))]
              (assoc collector ont-id 
                   (inc (cond (collector ont-id) (collector ont-id)
                              :t 0)))))
            {} (triples-from-files) ))

(defn run-ontology-id-frequency "" [] 
  (let [results (ontology-id-frequency)]
    (doseq [x 
          (into (sorted-map-by (fn [key1 key2]
	                         (compare [(get results key2) key2]
	                                  [(get results key1) key1])))
                results)]
    (println x))))

;;;;;;;;;;

(defn onotology-id-pos-distribution "" [])

(defn ontology-id-dep-type-distribution 
"just the roots for now" []
  (reduce (fn [collector item ] 
            (let [ont-id (first (nth item 2))]
              (assoc collector ont-id 
                   (inc (cond (collector ont-id) (collector ont-id)
                              :t 0)))))
            {} (filter (fn [x] (.equals (second x) "ROOT")) (triples-from-files))))

(defn run-sorted-xx "" [] 
  (let [results (ontology-id-dep-type-distribution)]
    (doseq [x 
          (into (sorted-map-by (fn [key1 key2]
	                         (compare [(get results key2) key2]
	                                  [(get results key1) key1])))
                results)]
    (println x))))

(defn list-roots "" [] 
  (doseq [x  (filter (fn [x] (.equals (second x) "ROOT")) 
                     (triples-from-files))]
    (println x)))
