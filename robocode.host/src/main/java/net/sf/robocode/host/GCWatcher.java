package net.sf.robocode.host;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Set;

public class GCWatcher {
	private Thread thread;
	private Set<Reference<?>> itemReferences = new HashSet<Reference<?>>();
	private ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
	
	public GCWatcher() {
		this.thread = new Thread("GC-Watcher"){
			@Override
			public void run() {
				GCWatcher.this.run();
			}
		};
		
		this.thread.setDaemon(true);
		this.thread.start();
	}
	
	public void watch(Object o){
		watch(o, null);
	}
	
	public void watch(Object o, String additionalInfo){
		MyReference r = new MyReference(o, referenceQueue, additionalInfo);
		System.out.println("### watching "+r);
		itemReferences.add(r);
	}
	
	protected void run() {
		System.out.println("### GC-Watcher active");
		
		try {
			while (true) {
				MyReference r = (MyReference) referenceQueue.remove();
				r.clear();
				System.out.println("### GC reclaimed "+r);
				itemReferences.remove(r);
			}
		} catch (InterruptedException ignored) {}
	}
	
	protected static class MyReference extends PhantomReference<Object> {
		
		public final String className;
		public final int identityHashCode;
		public final String additionalInfo;
		
		public MyReference(Object referent, ReferenceQueue<? super Object> q, String additionalInfo) {
			super(referent, q);
			this.className = referent.getClass().getName();
			this.identityHashCode = System.identityHashCode(referent);
			this.additionalInfo = additionalInfo;
		}
		
		@Override
		public String toString() {
			if( additionalInfo == null )
				return className + "@" + Integer.toHexString(identityHashCode);
			else
				return className + "@" + Integer.toHexString(identityHashCode) + " (" + additionalInfo + ")";
		}
	}
}
