;;
;; Copyright Christophe Roeder, August 2014


(ns clojure-craft.analyze-terms)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])

(use '[clojure-craft.craft-pos])
(use '[clojure-craft.craft-xml])
(use '[clojure-craft.craft-dep])
(use '[clojure-craft.craft-unify])

(defn- run-unify-pos-def [id]
  (let [txtfile (str txt-base "/" id ".txt")
        posfile (str pos-base "/" id ".txt.xml")
        depfile (str dep-base "/" id ".dep")]
  (unify-pos-dep txtfile posfile depfile)))

(defn- annotated-sentence? [sentence]
  (reduce (fn [collector token]
            (or collector (:anno-list token)))
          false (:tokens sentence)))

(defn- ontologies-used [sentence]
  (reduce (fn [collector token]
            (cond (:anno-list token)
                  (conj collector 
                        (list 
                         (:pos (:dependency token))
                         (:dep-type (:dependency token))
                              (:anno-list token)))
                  :t  collector))
          [] (:tokens sentence)))

(defn- get-type-pos-dep [sample-id]
  (let [base (run-unify-pos-def sample-id) 
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
           (cond (annotated-sentence? sentence) 
                 (ontologies-used sentence)
                 :t 
                 nil))
         data)) )


(defn create-ontology-pos-dep-triples
"create a list of ontology id, dep-type, pos-type"
[sample-id]
  (reduce (fn [collector item]
            (cond (> (count item) 0)
                  (reduce conj collector item)
                  :t
                  collector))
         [] (get-type-pos-dep sample-id)
         ))

(defn create-triple-map
"a triple here is an ontology-id, a part-of-speech, and a dependency type"
[triples]
  (sort (loop [triple (first triples)
         remaining (rest triples)
         triple-map {}]
    (cond (> (count triple) 2)
      (let [ontology-id (first (nth triple 2))
          new-map 
          (assoc triple-map ontology-id 
                          (conj (triple-map ontology-id)
                                (first triple) (second triple)))]
          (cond (not (empty? remaining))
                (recur (first remaining) (rest remaining) new-map)
                :t
                new-map))
      :t  triple-map  )) ))

(def id-list [
11532192,  15207008,  15876356,  16362077  17022820
11597317  15314655  15917436  16433929  17069463
11897010  

15921521  16462940  17078885
12079497  15320950  15938754  16504143  17083276
12546709  15328533  16098226  16504174  
12585968  15345036  16103912  16507151  17244351
12925238  15492776  16109169  16539743  17425782
14609438  15550985  16110338  16579849  17447844
15588329  16121255  16628246  17590087
14723793  15630473  16121256  16670015  17608565
14737183  15676071  16216087  16700629  17696610
15005800  
16221973  16870721
15040800  15819996  16255782  17002498
15061865  15836427  16279840  17020410
;15314659  
;17194222
;14611657  
;15760270  

])

(defn run-files
"" []
(doseq [x
  (reduce (fn [collector id] 
            (println "looking t od o " id)
         (merge collector (create-triple-map (create-ontology-pos-dep-triples id))))
       {} id-list) ]
  (println x)
))
