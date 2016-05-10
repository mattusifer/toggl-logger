(ns toggl-logger.core
  (:gen-class)
  (:require [toggl-logger.toggl :as toggl]
            [clojure.tools.cli :as cmd]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [cli-opts [[nil "--start CID" 
                   "Start a time entry corresponding to a CID - returns the ENTRY_ID"]
                  [nil "--stop ENTRY_ID" "Stop an existing time entry"]
                  ["-g" "--tag TAG" "Tag applied to access token"]
                  ["-n" "--name NAME" "The name of the client"]
                  ["-t" "--token TOKEN" "Your toggl access token"]
                  ["-d" "--desc DESC" "A description for your time entry"]]
        error-msg (fn [errors]
                    (str "The following errors occurred while parsing your command: \n\n"
                         (clojure.string/join \newline errors)))
        exit (fn [status msg]
               (println msg)
               (System/exit status))
        usage-summary (fn [summary]
                        (->> ["Use this program to start and stop toggl time entries."
                              ""
                              "Usage: toggl-logger [options]"
                              ""
                              "Options:"
                              summary]
                             (clojure.string/join \newline)))
        ;; parsed options
        {:keys [options arguments errors summary] :as res-map} (cmd/parse-opts args cli-opts 
                                                                               :in-order true)]
    
    (cond
      (empty? options) (exit 1 (usage-summary summary))
      (or (not (contains? options :token))
          (nil? (:token options)))
      (exit 1 (error-msg ["You need to include your token."]))
      (and (contains? options :start)
           (not (contains? options :desc))) 
      (exit 1 (error-msg ["You need to include a description if you're starting a time entry."]))
      (and (contains? options :start)
           (not (contains? options :name)))
      (exit 1 (error-msg ["You need to include the name of the client in case the project doesn't exist"]))
      errors (exit 1 (error-msg errors)))

    ;; main
    (case (first (keys options))
      :start
      (println (toggl/start (:start options) (:name options) 
                            (:desc options) (:tag options) (:token options)))
      :stop
      (if (toggl/stop (:stop options) (:token options))
        (println "Stopped toggl entry.")))))
