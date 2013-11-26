/**
 * @author Juan Rio Sipayung aka Joielechong
 * Medan, Sumatera Utara, Indonesia.
 *
 */
package com.joielechong.ributer;

public interface OnErrorListener {

	public void onError(final String msg);

	public void onError(final Exception exc);

	public void onNotRoot();
}