(ns todo.infrastructure.parse)

(defn ->int [x] 
  (if (number? x) 
    x 
    (try 
      (Integer/parseInt x)
      (catch NumberFormatException _ x))))

 
