package com.intel.director.util;

import java.util.Comparator;

import com.intel.director.api.TrustPolicy;

public class TrustPolicyComparator implements Comparator<TrustPolicy>{

	@Override
	public int compare(TrustPolicy o1, TrustPolicy o2) {
		return o1.getEdited_date().compareTo(o2.getEdited_date());
	}

}
