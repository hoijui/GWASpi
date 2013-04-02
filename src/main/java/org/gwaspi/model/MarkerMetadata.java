package org.gwaspi.model;


import org.gwaspi.global.TypeConverter;

public class MarkerMetadata {

	public static final TypeConverter<MarkerMetadata, String> TO_MARKER_ID
			= new TypeConverter<MarkerMetadata, String>()
	{
		@Override
		public String convert(MarkerMetadata from) {
			return from.getMarkerId();
		}
	};

	public static final TypeConverter<MarkerMetadata, String> TO_RS_ID
			= new TypeConverter<MarkerMetadata, String>()
	{
		@Override
		public String convert(MarkerMetadata from) {
			return from.getRsId();
		}
	};

	public static final TypeConverter<MarkerMetadata, String> TO_CHR
			= new TypeConverter<MarkerMetadata, String>()
	{
		@Override
		public String convert(MarkerMetadata from) {
			return from.getChr();
		}
	};

	public static final TypeConverter<MarkerMetadata, Integer> TO_POS
			= new TypeConverter<MarkerMetadata, Integer>()
	{
		@Override
		public Integer convert(MarkerMetadata from) {
			return from.getPos();
		}
	};

	public static final TypeConverter<MarkerMetadata, String> TO_ALLELES
			= new TypeConverter<MarkerMetadata, String>()
	{
		@Override
		public String convert(MarkerMetadata from) {
			return from.getAlleles();
		}
	};

	public static final TypeConverter<MarkerMetadata, String> TO_STRAND
			= new TypeConverter<MarkerMetadata, String>()
	{
		@Override
		public String convert(MarkerMetadata from) {
			return from.getStrand();
		}
	};

	private final String markerId;
	private final String rsId;
	private final String chr;
	private final int pos;
	private final String alleles;
	private final String strand;

	public MarkerMetadata(
			String markerId,
			String rsId,
			String chr,
			int pos,
			String alleles,
			String strand)
	{
		this.markerId = markerId;
		this.rsId = rsId;
		this.chr = chr;
		this.pos = pos;
		this.alleles = alleles;
		this.strand = strand;
	}

	public MarkerMetadata(
			String markerId,
			String rsId,
			String chr,
			int pos,
			String alleles)
	{
		this(markerId, rsId, chr, pos, alleles, null);
	}

	public MarkerMetadata(
			String markerId,
			String rsId,
			String chr,
			int pos)
	{
		this(markerId, rsId, chr, pos, null);
	}

	public MarkerMetadata(
			String chr,
			int pos)
	{
		this(null, null, chr, pos, null);
	}

	public String getMarkerId() {
		return markerId;
	}

	public String getRsId() {
		return rsId;
	}

	public String getChr() {
		return chr;
	}

	public int getPos() {
		return pos;
	}

	public String getAlleles() {
		return alleles;
	}

	public String getStrand() {
		return strand;
	}
}
