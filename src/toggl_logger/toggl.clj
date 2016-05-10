(ns toggl-logger.toggl
  (:require [clj-http.client :as client]
            [clojure.data.codec.base64 :as b64]
            [cheshire.core :as json]))

(def token (atom nil))

(def base-url "https://www.toggl.com/api/v8")

(def endpoints {:start "/time_entries/start"
                :stop "/time_entries/{time_entry_id}/stop"
                :workspace-projects "/workspaces/{workspace_id}/projects"
                :workspace-tags "/workspaces/{workspace_id}/tags"
                :projects "/projects"})

(defn get-config 
  "deref token into config"
  []
  {:headers {:Authorization (str "Basic " @token)
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

(defn- fill-workspace-id
  [string id]
  (clojure.string/replace string #"\{workspace_id\}" (str id)))

(defn get-toggl-project
  "get project from CID"
  [cid cname]
  (let [projects
        (json/parse-string (:body (client/get (fill-workspace-id (str base-url (:workspace-projects endpoints)) analysts-wid) (get-config)))
                           true)]

    (if-let [project (some #(when (re-find (re-pattern (str cid " \\(.*\\)")) (:name %)) %) projects)]
      (:id project)
      (let [project (json/parse-string 
                     (:body (client/post (str base-url
                                              (:projects endpoints))
                                         (-> (get-config)
                                             (assoc :form-params
                                                    {:project {:name (str cid " (" cname ")")
                                                               :wid analysts-wid
                                                               :is-private false}}
                                                    :content-type "application/json")))))]
        (:id project)))))

(defn get-tag-name
  "get correct tag name based on input"
  [tag-name]
  (let [tags 
        (json/parse-string (:body (client/get 
                                   (fill-workspace-id (str base-url (:workspace-tags endpoints)) 
                                                      analysts-wid) (get-config))) true)]
    (some #(when (= (clojure.string/lower-case tag-name) 
                    (clojure.string/lower-case (:name %))) (:name %))
          tags)))

(defn start 
  "start a new time entry under the 'client support' project for a client - returns the id"
  [cid cname description tag-name token-str]
  (reset! token (String. (b64/encode (.getBytes token-str))))
  (let [pid (get-toggl-project cid cname)
        tag (get-tag-name tag-name)
        result (client/post (str base-url (:start endpoints)) 
                            (-> (get-config) 
                                (assoc :form-params 
                                       {:time_entry {:description description
                                                     :tags [tag]
                                                     :pid pid
                                                     :created_with "matt's automated script"}}
                                       :content-type "application/json")))]
    (:id (:data (json/parse-string (:body result) true)))))

(defn stop 
  "stop an existing time entry"
  [time-entry-id token-str]
  (reset! token (String. (b64/encode (.getBytes token-str))))
  (client/put (fill-time-entry-id (str base-url (:stop endpoints)) time-entry-id)
              (get-config)))
