/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

var Signposts;

(function ($) {

  Signposts = Class.extend({

    $element: $('#almSignposts'),

    init: function () {
      var that = this;

      var counter_query = new CounterQuery();
      var counter_validator = new CounterQueryValidator();

      var counter_views;

      counter_query
        .setDataValidator(counter_validator)
        .getArticleSummary(ArticleData.doi)
        .then(function (counterData) {
          counter_views = counterData['totals'];
          call_alm(counter_views, that);
        })
        .fail(function (error) {
          show_error(error, that);
        });

    }
  });

  function call_alm(counter_views, that) {
    var alm_query = new AlmQuery();
    var alm_validator = new AlmQueryValidator({checkSources: false});

    alm_query
      .setDataValidator(alm_validator)
      .getArticleDetail(ArticleData.doi)
      .then(function (articleData) {
        var data = articleData[0];
        var template = _.template($('#signpostsTemplate').html());
        var templateData = {
          saveCount: data.saved,
          citationCount: data.cited,
          shareCount: data.discussed,
          viewCount: getPmcViewsAndDownloads(articleData) + counter_views
        };

        that.$element.html(template(templateData));

        if (!_.isUndefined(data.sources)) {
          var scopus = _.findWhere(data.sources, {name: 'scopus'});
          if (scopus.metrics.total > 0) {
            $('#almCitations').find('.citations-tip a').html('Displaying Scopus citation count.');
          }
        }

        //Initialize tooltips
        tooltip_hover.init();
      })
      .fail(function (error) {
        show_error(error, that);
      });
  }

  function show_error(error, that) {
    var template;
    switch (error.name) {
      case 'NewArticleError':
        template = _.template($('#signpostsNewArticleErrorTemplate').html());
        break;
      default:
        template = _.template($('#signpostsGeneralErrorTemplate').html());
        break;
    }
    that.$element.html(template());
  }

  new Signposts();

})(jQuery);

function getPmcViewsAndDownloads(data) {
  //todo: replace YUI compressor so we can use the following line instead of hardcoding '7'
  //https://github.com/yui/yuicompressor/issues/262
  // var pmc_metrics = data[0].sources.find(x => x.name === 'pmc' && x.group_name === 'viewed');
  var pmc_metrics = data[0].sources[7]['metrics'];
  return pmc_metrics['html'] + pmc_metrics['pdf'];
}