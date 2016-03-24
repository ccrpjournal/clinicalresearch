/*

  AlmQuery

  Usage reference:
  var query = new AlmQuery();

  query.getMediaReferences(['10.1371/journal.pone.0144197', '10.1371/journal.pone.0151227'])
  .then(function (almData) {

  })
  .fail(function (error) {

  });

 */

var AlmQuery;

(function ($) {
  AlmQuery = Class.extend({
    /*
     * Constructor function
     */

    init: function (config) {
      this.config = _.extend(ALM_CONFIG, config);
    },
    
    /*
     * DOI format and validation functions
     */

    isDOIValid: function (doi) {
      return !_.isEmpty(doi) && (_.isString(doi) || _.isArray(doi));
    },

    formatDOI: function (doi) {
      doi = encodeURI(doi);
      return doi.replace(new RegExp('/', 'g'), '%2F').replace(new RegExp(':', 'g'), '%3A');
    },

    validateDOI: function (doi) {
      var that = this;
      if(this.isDOIValid(doi)) {
        if(_.isString(doi)) {
          return this.formatDOI(doi);
        }
        else if (_.isArray(doi)) {
          return _.map(doi, function (value) { return that.formatDOI(value); });
        }
      }
      else {
        throw new ErrorFactory('InvalidDOIError', '[AlmQuery::validateDOI] - Invalid DOI');
      }
    },
    
    /*
     * Request handling functions
     */

    getRequestBaseUrl: function () {
      return this.config.host + '?api_key=' + this.config.apiKey;
    },

    getRequestUrl: function (queryParams) {
      if(_.has(queryParams, 'ids')) {
        queryParams.ids = this.validateDOI(queryParams.ids);
      }

      queryParams = _.reduce(queryParams, function (memo, value, index) {
        if(_.isArray(value)) {
          value = _.reduce(value, function (memo, value) { return memo + value + ','; }, '');
          value = value.slice(0, -1);
        }

        return memo + '&' + index + '=' + value;
      }, '');

      return this.getRequestBaseUrl() + queryParams;
    },

    processRequest: function (requestUrl) {
      var deferred = Q.defer();

      if(this.config.host == null) {
        var err = new Error('[AlmQuery::processRequest] - ALM API is not configured');
        err.name = 'ALMNotConfiguredError';
        deferred.reject(new ErrorFactory('ALMNotConfiguredError', '[AlmQuery::processRequest] - ALM API is not configured'));
      }
      else {
        $.ajax({
          url: requestUrl,
          jsonp: 'callback',
          dataType: 'jsonp',
          timeout: 20000,
          success: function (response) {
            deferred.resolve(response);
          },
          error: function (jqXHR, textStatus) {
            var err = new Error('[AlmQuery::processRequest] - Request failed to API');
            err.name = 'APIRequestError';
            deferred.reject(new ErrorFactory('APIRequestError', '[AlmQuery::processRequest] - Request failed to API'));
          }
        });
      }

      return deferred.promise;
    },

    /*
     * API Request methods:
     */

    getArticleSummary: function (doi) {
      var requestUrl = this.getRequestUrl({
        ids: doi
      });

      return this.processRequest(requestUrl);
    },

    getArticleDetail: function (doi) {
      var requestUrl = this.getRequestUrl({
        ids: doi,
        info: 'detail'
      });

      return this.processRequest(requestUrl);
    },

    getMediaReferences: function (doi) {
      var requestUrl = this.getRequestUrl({
        ids: doi,
        source_id: 'articlecoveragecurated',
        info: 'detail'
      });

      return this.processRequest(requestUrl);
    }
  });



})(jQuery);