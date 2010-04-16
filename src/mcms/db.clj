(ns mcms.db)

(defn next-id 
  ([table]
     ["count" table])
  ([db table]
     (inc (db (next-id table)))))