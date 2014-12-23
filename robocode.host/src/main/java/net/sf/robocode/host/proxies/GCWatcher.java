package net.sf.robocode.host.proxies;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Set;

class GCWatcher extends Thread {
	Set<Reference<?>> allCLRefs = new HashSet<Reference<?>>();
	ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
	
	public GCWatcher() {
		setName("GC-Watcher");
		setDaemon(true);
	}
	
	public void run() {
		System.out.println("### GC-Watcher active");
		
		try {
			while (true) {
				MyReference r = (MyReference) refQueue.remove();
				r.clear();
				System.out.println("### GC reclaimed "+r);
				allCLRefs.remove(r);
			}
		} catch (InterruptedException ignored) {}
	}
	
	public void watch(Object o){
		MyReference r = new MyReference(o, refQueue);
		System.out.println("### watching "+r);
		allCLRefs.add(r);
	}
	
	private static class MyReference extends PhantomReference<Object> {
		
		public final String className;
		public final int identityHashCode;
		
		public MyReference(Object referent, ReferenceQueue<? super Object> q) {
			super(referent, q);
			this.className = referent.getClass().getName();
			this.identityHashCode = System.identityHashCode(referent);
		}
		
		@Override
		public String toString() {
			return className + "@" + Integer.toHexString(identityHashCode);
		}
	}
}