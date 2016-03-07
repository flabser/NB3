package kz.lof.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class MapperMixIn {
	@JsonIgnore
	int stackTrace;
}
