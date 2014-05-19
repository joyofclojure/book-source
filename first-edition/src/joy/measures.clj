(ns joy.measures
  "Units of measure conversion DSL from section 13.1")

(defn relative-units [u units history]
  (if (some #{u} history)
    (throw (Exception. (str "Cycle in " u " and " history))))
  (let [spec (u units)]
    (if (nil? spec)
      (throw (Exception. (str "Undefined unit " u)))
      (if (vector? spec)
        (let [[conv to] spec]
          (* conv
             (relative-units to units (conj history u))))
          spec))))

(defmacro defunits-of [quantity base-unit & units]
  (let [magnitude (gensym)
        unit (gensym)
        conversions (into `{~base-unit 1} (map vec (partition 2 units)))]
    `(defmacro ~(symbol (str "unit-of-" quantity)) [~magnitude ~unit]
       `(* ~~magnitude
           ~(case ~unit
                  ~@(mapcat
                     (fn [[u# & r#]]
                       `[~u# ~(relative-units u# conversions [])])
                     conversions))))))

(comment
  (defunits-of distance :m
    :km 1000
    :cm 1/100
    :mm [1/10 :cm]
    :nm [1/1000 :mm]

    :yard 9144/10000
    :foot [1/3 :yard]
    :inch [1/12 :foot]
    :mile [1760 :yard]
    :furlong [1/8 :mile]

    :fathom [2 :yard]
    :nautical-mile 1852
    :cable [1/10 :nautical-mile]

    :old-brit-nautical-mile [6080/3 :yard]
    :old-brit-cable [1/10 :old-brit-nautical-mile]
    :old-brit-fathom [1/100 :old-brit-cable]))
