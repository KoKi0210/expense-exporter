package com.example.expenseexporter.export;

/**
 * <p>Factory Method: {@link #createExporter()} is abstract and every constant overrides it, so each
 * format decides which {@link InvoiceExporter} subclass it creates. Callers only see the abstract
 * {@link InvoiceExporter}. A new constant does not compile until it implements the method.
 */
public enum ExportFormat {
  CSV("text/csv", "csv") {
    @Override
    public InvoiceExporter createExporter() {
      return new CsvExporter();
    }
  },
  JSON("application/json", "json") {
    @Override
    public InvoiceExporter createExporter() {
      return new JsonExporter();
    }
  },
  XML("application/xml", "xml") {
    @Override
    public InvoiceExporter createExporter() {
      return new XmlExporter();
    }
  };

  private final String mediaType;
  private final String fileExtension;

  ExportFormat(String mediaType, String fileExtension) {
    this.mediaType = mediaType;
    this.fileExtension = fileExtension;
  }

  public abstract InvoiceExporter createExporter();

  public String mediaType() {
    return mediaType;
  }

  public String fileExtension() {
    return fileExtension;
  }
}
