(ns hyphen-import
  (:require [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(def table "words_new")
(def spelling "1")

(defn read-csv [file]
  (with-open [reader (io/reader file)]
    (doall
     (csv/read-csv reader))))

(defn escape-sql [s]
  (-> s (string/replace #"'" "''")))

(defn to-sql [record]
  (let [[word hyphenation] (map escape-sql record)]
    (format "(\"%s\", \"%s\", %s)" word hyphenation, spelling)))

(defn -main [& args]
  (let [file (first args)
        records (->>
                 file
                 read-csv
                 (map to-sql))]
    (println "START TRANSACTION;")
    (println (format "INSERT INTO %s (word,hyphenation,spelling)" table))
    (println "VALUES")
    (println (string/join ",\n" records))
    (println "ON DUPLICATE KEY UPDATE hyphenation=VALUES(hyphenation);")
    (println "COMMIT;")))
