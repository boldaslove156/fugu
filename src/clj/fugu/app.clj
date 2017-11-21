(ns fugu.app
  (:require
   [mount.core :as mnt]
   [fugu.system.config]
   [fugu.system.db]
   [fugu.system.db.bootstrapper]
   [fugu.system.db.listener]
   [fugu.system.db.listener.register]
   [fugu.system.dispatcher]
   [fugu.system.dispatcher.listener]
   [fugu.system.dispatcher.listener.register]
   [fugu.system.ws]
   [fugu.system.ws.listener]
   [fugu.system.ws.listener.register]
   [fugu.system.router]
   [fugu.system.web]))

(mnt/in-cljc-mode)
