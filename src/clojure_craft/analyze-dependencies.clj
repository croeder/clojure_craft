;;
;; Copyright Christophe Roeder, August 2014

;; This namespace contains functions analyzing or counting 
;; the sentences and the dependencies within them.


(ns clojure-craft.analyze-dependencies)
(use 'clojure.java.io)
(use '[clojure.string :only (join split)])

(use '[clojure-craft.craft-pos])
(use '[clojure-craft.craft-xml])
(use '[clojure-craft.craft-dep])
(use '[clojure-craft.craft-unify])


(def id-list [
11532192  15207008  15876356  16362077  17022820
11597317  15314655  15917436  16433929  17069463
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

;(defrecord Sentence [filename  sentence-number text start end tokens] )
;(defrecord Token [token-number part-of-speech text start end dependency anno-list] )


(defn map-sentence-to-stats [sentence]
  (reduce 
   (fn [stats-map token]
     (let [ann (:anno-list token)
           dep (:dependency token)
           dt  (:dep-type dep)]   
;       (cond (and (= "ROOT" dt) (not (empty? ann)))
;               (assoc stats-map (first ann) (inc (stats-map (first ann)))))
;      :t  stats-map))
       (assoc stats-map  (str "xx" token)
                              (cond (stats-map (first ann))
                                           (inc (stats-map (first ann)))
                                           :t   1)) ))
   {} (:tokens sentence)))


(defn map-sentence-list-to-stats [sentences]
  (println "num sentences:"  (count sentences))
  (reduce (fn [map sentence]
            (println "sentence-num:" (:sentence-number sentence))
            (merge map (map-sentence-to-stats sentence)))
          {} sentences))

(defn map-docs-to-stats []
  (reduce (fn [map doc-id]
            (println "doc-id:" doc-id)
            (merge map
                   (map-sentence-list-to-stats 
                    (take 5 (run-unify-pos-dep doc-id)))))
   {} (take 5 id-list)))

