package org.ambraproject.wombat.model;

import java.util.List;
import java.util.Map;

public class SearchFilterItem {

  private final String displayName;
  private final float numberOfHits;
  private final String filterParamName;
  private final String filterValue;
  private final Map<String, List<String>> filteredResultsParameters;

  public SearchFilterItem(String displayName, float numberOfHits, String filterParamName,
      String filterValue, Map<String, List<String>> filteredResultsParameters) {
    this.displayName = displayName;
    this.numberOfHits = numberOfHits;
    this.filterParamName = filterParamName;
    this.filterValue = filterValue;
    this.filteredResultsParameters = filteredResultsParameters;
  }

  public String getDisplayName() {
    return displayName;
  }

  public float getNumberOfHits() {
    return numberOfHits;
  }

  public String getFilterParamName() {
    return filterParamName;
  }

  public String getFilterValue() {
    return filterValue;
  }

  public Map<String, List<String>> getFilteredResultsParameters() {
    return filteredResultsParameters;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SearchFilterItem)) return false;

    SearchFilterItem that = (SearchFilterItem) o;

    if (Float.compare(that.numberOfHits, numberOfHits) != 0) return false;
    if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
    if (filterParamName != null ? !filterParamName.equals(that.filterParamName) : that.filterParamName != null) {
      return false;
    }
    if (filterValue != null ? !filterValue.equals(that.filterValue) : that.filterValue != null) return false;
    if (filteredResultsParameters != null ? !filteredResultsParameters.equals(that.filteredResultsParameters) : that.filteredResultsParameters != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = displayName != null ? displayName.hashCode() : 0;
    result = 31 * result + (numberOfHits != +0.0f ? Float.floatToIntBits(numberOfHits) : 0);
    result = 31 * result + (filterParamName != null ? filterParamName.hashCode() : 0);
    result = 31 * result + (filterValue != null ? filterValue.hashCode() : 0);
    result = 31 * result + (filteredResultsParameters != null ? filteredResultsParameters.hashCode() : 0);
    return result;
  }

}
