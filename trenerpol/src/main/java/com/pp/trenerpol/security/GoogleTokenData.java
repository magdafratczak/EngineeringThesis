package com.pp.trenerpol.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class GoogleTokenData implements Serializable {
	private String qrCode;
	private String mfaCode;
}