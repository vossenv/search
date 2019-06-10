package com.dm.search.preprocessor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchParameters {

    private String query;
    private Pageable pageable;
    private String filter;
    private int fuzziness;

    public static class Builder {
        private String query = "";
        private Pageable pageable = PageRequest.of(0, 100);
        private String filter = "";
        private int fuzziness = 3;

        public Builder() {}

        public Builder (String query) {
            this.query = query;
        }

        public Builder withPageable(Pageable pageable) {
            this.pageable = pageable;
            return this;
        }

        public Builder withQuery(String query) {
            this.query = query;
            return this;
        }

        public Builder withFilter(String filter) {
            this.filter = filter;
            return this;
        }

        public Builder withfuzziness(int fuzziness) {
            this.fuzziness = fuzziness;
            return this;
        }

        public Builder incDisabled(boolean disabled){
            this.filter = !disabled ? "enabled:true" : this.filter;
            return this;
        }

        public Builder enabledOnly(){
            this.filter = "enabled:true";
            return this;
        }

        public Builder disabledOnly(){
            this.filter = "enabled:false";
            return this;
        }

        public Builder withPageSize(int size){
            this.pageable = PageRequest.of(0, size);
            return this;
        }

        public SearchParameters build() {

            SearchParameters s = new SearchParameters();
            s.filter = this.filter;
            s.fuzziness = this.fuzziness;
            s.pageable = this.pageable;
            s.query = this.query;
            return s;
        }
    }
}
