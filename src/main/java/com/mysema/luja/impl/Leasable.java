package com.mysema.luja.impl;

public interface Leasable {

	/**
	 * Leases, returns true if lease is successful
	 * @return
	 */
    boolean lease();

    /**
     * Releases the lease
     */
    void release();

}
