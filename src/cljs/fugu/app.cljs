(ns fugu.app
  (:require
   [mount.core :as mnt]
   [fugu.system.ws]
   [fugu.system.ws.listener]
   [fugu.system.ws.listener.register]
   [fugu.system.dispatcher]
   [fugu.system.dispatcher.listener]
   [fugu.system.dispatcher.listener]
   [fugu.system.dispatcher.listener.register]
   [fugu.system.db]
   [fugu.system.db.listener]
   [fugu.system.db.listener.register]
   [fugu.system.element]
   [fugu.system.element.listener.register]))

(enable-console-print!)

(mnt/start)
