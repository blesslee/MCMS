(use 'fleetdb.client)

(def client (connect {:host "127.0.0.1", :port 3400}))

(client ["ping"])

(client ["insert" "collection" [{:id 2, :owner "remleduff", :title "The Wheel of Time"}
				{:id 3, :owner "remleduff", :title "Programming Clojure"}
				{:id 4, :owner "remleduff", :title "Have Spacesuit Will Travel"}]])

(client ["select" "collection" {"where" ["=" :owner "remleduff"]}])

