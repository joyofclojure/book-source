(ns joy.elevator
  "The elavator state machine from section 7.3.3 using a trampoline.")

(defn elevator [commands]
  (letfn                    ;; #: Local functions
      [(ff-open [[cmd & r]]   ;; #: 1st floor open
         "When the elevator is open on the 1st floor
          it can either close or be done."
         #(case cmd
                :close (ff-closed r)
                :done  true
                false))
       (ff-closed [[cmd & r]] ;; #: 1st floor closed
         "When the elevator is closed on the 1st floor
          it can either open or go up." 
         #(case cmd
                :open (ff-open r)
                :up   (sf-closed r)
                false))
       (sf-closed [[cmd & r]] ;; #: 2nd floor closed
         "When the elevator is closed on the 2nd floor 
          it can either go down or open."
         #(case cmd
                :down (ff-closed r)
                :open (sf-open r)
                false))
       (sf-open [[cmd & r]] ;; #: 2nd floor open
         "When the elevator is open on the 2nd floor
          it can either close or be done"
         #(case cmd
                :close (sf-closed r)
                :done  true
                false))]
    (trampoline ff-open commands)))
