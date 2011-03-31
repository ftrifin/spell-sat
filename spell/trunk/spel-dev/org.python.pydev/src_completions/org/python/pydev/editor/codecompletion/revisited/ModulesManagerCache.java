package org.python.pydev.editor.codecompletion.revisited;

import org.python.pydev.core.ModulesKey;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.cache.LRUCache;
import org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule;

/**
 * This is a 'global' cache implementation, that can have at most n objects in
 * the memory at any time.
 */
final class ModulesManagerCache  {
    /**
     * Defines the maximun amount of modules that can be in the memory at any time (for all the managers)
     */
    private static final int MAX_NUMBER_OF_MODULES = 400;

    /**
     * The access to the cache is synchronized
     */
    LRUCache<Tuple<ModulesKey, ModulesManager>, AbstractModule> internalCache;
    
    ModulesManagerCache() {
        internalCache = new LRUCache<Tuple<ModulesKey, ModulesManager>, AbstractModule>(MAX_NUMBER_OF_MODULES);
    }
    
    /**
     * Overriden so that if we do not find the key, we have the chance to create it.
     */
    public AbstractModule getObj(ModulesKey key, ModulesManager modulesManager) {
        synchronized (modulesManager.modulesKeys) {
            Tuple<ModulesKey, ModulesManager> keyTuple = new Tuple<ModulesKey, ModulesManager>(key, modulesManager);
            
            AbstractModule obj = internalCache.getObj(keyTuple);
            if(obj == null && modulesManager.modulesKeys.containsKey(key)){
                key = modulesManager.modulesKeys.get(key); //get the 'real' key
                obj = AbstractModule.createEmptyModule(key);
                internalCache.add(keyTuple, obj);
            }
            return obj;
        }
    }

    public void remove(ModulesKey key, ModulesManager modulesManager) {
        synchronized (modulesManager.modulesKeys) {
            Tuple<ModulesKey, ModulesManager> keyTuple = new Tuple<ModulesKey, ModulesManager>(key, modulesManager);
            internalCache.remove(keyTuple);
        }
    }

    public void add(ModulesKey key, AbstractModule n, ModulesManager modulesManager) {
        synchronized (modulesManager.modulesKeys) {
            Tuple<ModulesKey, ModulesManager> keyTuple = new Tuple<ModulesKey, ModulesManager>(key, modulesManager);
            internalCache.add(keyTuple, n);
        }
    }
}