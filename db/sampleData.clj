(use 'fleetdb.client)

(def client (connect {:host "127.0.0.1", :port 3400}))

(client ["ping"])

; ID = ISBN
(client ["insert" "media" [{:id 0450050777, :title "Stranger in a Strange Land", :author "Heinlein, Robert"}
			   {:id 1416505490, :title "Have Spacesuit Will Travel", :author "Heinlein, Robert"}
			   {:id 1934356336, :title "Programming Clojure", :author "Halloway, Stuart"}]])

(client ["insert" "users" [{:id 1, :name "remleduff"}]])

; id = unique number
(client ["insert" "collection" [{:id 1, :owner 1, :isbn 1416505490}
				{:id 2, :owner 1, :isbn 0450050777}
				{:id 3, :owner 1, :isbn 1934356336}]])

; id = ISBN
(client ["insert" "covers" {:id 1416505490, :cover "binary data"}])

(client ["delete" "media"])
(client ["delete" "users"])
(client ["delete" "collection"])
(client ["delete" "covers"])

(client ["select" "collection" {"where" ["=" :owner 1]}])
