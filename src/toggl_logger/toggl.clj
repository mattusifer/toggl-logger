(ns toggl-logger.toggl
  (:require [clj-http.client :as client]
            [clojure.data.codec.base64 :as b64]
            [cheshire.core :as json]))

(def token (String. (b64/encode (.getBytes "bccfaca9d9a964dd36c68dec5ff56f82:api_token"))))

(def base-url "https://www.toggl.com/api/v8")

(def endpoints {:start "/time_entries/start"
                :stop "/time_entries/{time_entry_id}/stop"
                :clients "/clients"
                :client-projects "/clients/{client_id}/projects"
                :projects "/projects"})

(def config {:headers {:Authorization (str "Basic " token)
                       :Accept "application/json"}
             ;; :debug true
             ;; :debug-body true
             })

;; analysts workspace ID 
(def analysts-wid 745721)

(defn- fill-client-id
  [string cid]
  (clojure.string/replace string #"\{client_id\}" (str cid)))

(defn- fill-time-entry-id
  [string id]
  (clojure.string/replace string #"\{time_entry_id\}" (str id)))

(defn get-toggl-cid
  "get client id - client will be created if it doesn't exist"
  [cid]
  (let [clients
        (json/parse-string (:body (client/get (str base-url (:clients endpoints)) config))
                           true)]

    (if-let [client (some #(when (= (:name %) cid) %) clients)]
      (:id client)
      (let [client (json/parse-string 
                    (:body (client/post (str base-url 
                                             (:clients endpoints))
                                        (-> config
                                            (assoc :form-params 
                                                   {:client {:name cid :wid analysts-wid}} 
                                                   :content-type "application/json")))) true)]
        (:id client)))))

(defn get-support-project-id
  "get project id for 'client support' - will be created if it doesn't exist"
  [cid]
  (let [toggl-client-id (get-toggl-cid cid)
        client-projects
        (json/parse-string 
         (:body (client/get (str base-url (fill-client-id (:client-projects endpoints) 
                                                          toggl-client-id))
                            config)) true)]

    (if-let [project (some #(when (= (clojure.string/lower-case (:name %)) "client support") %)
                           client-projects)]
      (:id project)

      ;; iterate through random case configurations until one has not been taken
      ;; and create it, returning the ID
      (loop [proj-config (-> config
                             (assoc :form-params
                                    {:project {:name "client support" :wid analysts-wid 
                                               :is_private false :cid cid}}
                                    :content-type "application/json"))]
        (let [result (try
                       (client/post (str base-url (:projects endpoints)) proj-config)
                       (catch RuntimeException e (ex-data e)))]
          (if (and (= (:status result) 400) (re-find #"already been taken" (:body result)))
            (let [[pref suff] 
                  (map #(reduce str %) 
                       (split-at (rand-int (count "client support")) "client support"))]
              (recur (assoc-in proj-config [:form-params :project :name]
                               (str (clojure.string/upper-case pref) suff))))
            (print (:body result))))))))

(defn start 
  "start a new time entry under the 'client support' project for a client - returns the id"
  [cid description]
  (let [pid (get-support-project-id cid)
        result (client/post (str base-url (:start endpoints)) 
                            (-> config 
                                (assoc :form-params 
                                       {:time_entry {:description description
                                                     :pid pid
                                                     :created_with "automated-script"}}
                                       :content-type "application/json")))]
    (:id (:data (json/parse-string (:body result) true)))))

(defn stop 
  "stop an existing time entry"
  [time-entry-id]
  (client/put (fill-time-entry-id (str base-url (:stop endpoints)) time-entry-id)
              config))


  (comment 

    (def cid (str 6115))

    (client 6115)

    (json/parse-string (:body (client/get (str base-url "/workspaces") config))
                       true)

    (client 6115)

    (def time-entry-id (start "6115"))



    )