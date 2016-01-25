
var FigureLightbox = {};
(function($) {

  FigureLightbox = {
    lbContainerSelector:     '#figure-lightbox-container',

    /* internal selectors */
    lbSelector:               '#figure-lightbox',
    lbTemplateSelector:       '#figure-lightbox-template',
    contextTemplateSelector:  '#image-context-template',
    lbCloseButtonSelector:    '.lb-close',
    zoomRangeSelector:        '.range-slider',
    $panZoomEl:               null,
    imgData:                  null,


    /* internal config variables */
    imgPath:        WombatConfig.figurePath || 'IMG_PATH_NOT_LOADED'
  };

  FigureLightbox.insertLightboxTemplate = function () {
    var articleData = this.fetchArticleData();

    var lbTemplate = _.template($(this.lbTemplateSelector).html());
    $(this.lbContainerSelector).append(lbTemplate(articleData));
    this.switchImage(this.imgData.doi);
  };

  FigureLightbox.fetchArticleData = function () {
    // @TODO: Do not parse article. Fetch data via an ajax call
    var $mainContainer = $(document).find('main');
    return {
      doi: this.imgData.doi,
      strippedDoi: this.imgData.strippedDoi,

      articleTitle: $mainContainer.find('#artTitle').text(),
      authorList: $mainContainer.find('.author-name').text(),
      figureList: $mainContainer.find('.lightbox-figure')
    };
  };

  FigureLightbox.fetchImageData = function () {
    return {
      doi: this.imgData.doi,
      strippedDoi: this.imgData.strippedDoi,

      title: this.imgData.imgElement.find('.figcaption').text(),
      description: this.imgData.imgElement.find('.caption_target').next().html()
    };
  };


  FigureLightbox.bindBehavior = function () {
    var that = this;
    // Escape key destroys and closes lightbox
    $(document).on('keyup.figure-lightbox', function(e) {
      if (e.keyCode === 27) {
        that.close();
      }
    });

    $(this.lbContainerSelector)
      // Bind close button
        .find(this.lbCloseButtonSelector).on('click', function () {
          that.close();
        }).end()

        // Bind buttons to change images
        .find('.change-img').on('click', function () {
          that.switchImage(this.getAttribute('data-doi'));
        }).end()

        // Bind button to show all images
        .find('.all-fig-btn').on('click', function () {
          var $figList = $('#figures-list');
          if (!$figList.is(':visible')) { // If not is visible show it
            $figList.show();
            var tmpPos = $figList.position();
            $figList.css({right: tmpPos.left - screen.width});
          }
          var end = $figList.position();
          // Get the static position
          $figList.animate({ // Animate toggle to the right
            right: end.left - screen.width
          });
        }).end()

        // Bind mousewheel in figure list. Prevent image zooming
        .find('#figures-list').on('mousewheel', function(e) {
          e.stopPropagation();
        }).end()

        // Bind show in context button
        .find('.show-context').on('click', function () {
          that.close();
        }).end()

        // Bind next figure button
        .find('.next-fig-btn').on('click', function () {
          return that.nextImage();
        }).end()

        // Bind next figure button
        .find('.prev-fig-btn').on('click', function () {
          return that.prevImage();
        });
  };

  FigureLightbox.nextImage = function () {
    var currentIx = this.getCurrentImageIndex();
    var nextImg = this.imgList[currentIx + 1];
    if (!nextImg) {
      return false;
    }
    this.switchImage(nextImg.getAttribute('data-doi'));
  };

  FigureLightbox.prevImage = function () {
    var currentIx = this.getCurrentImageIndex();
    var prevImg = this.imgList[currentIx - 1];
    if (!prevImg) {
      return false;
    }
    this.switchImage(prevImg.getAttribute('data-doi'));
  };

  FigureLightbox.getCurrentImageIndex = function () {
    var that = this;
    var currentIx = null;
    this.imgList.each(function (ix, img) {
      if (img.getAttribute('data-doi') === that.imgData.strippedDoi) {
        currentIx = ix;
        return false;
      }
    });
    return currentIx;
  };

  FigureLightbox.switchImage = function (imgDoi) {
    this.imgData = {
      doi: imgDoi
    };
    this.imgData.strippedDoi = this.imgData.doi.replace(/^info:doi\//, '');
    this.imgData.imgElement = this.imgList.filter('.figure[data-doi="' + this.imgData.strippedDoi + '"]');
    var imageData = this.fetchImageData();
    var templateFunctions = {
      showInContext: this.showInContext
    };
    var templateData = $.extend(imageData, templateFunctions);

    var lbTemplate = _.template($(this.contextTemplateSelector).html());
    // Remove actual img context
    $('#image-context').children().remove().end()
        // Append new img context
        .append(lbTemplate(templateData));

    this.renderImg(this.imgData.doi);
  };

  FigureLightbox.loadImage = function (lbContainer, imgDoi, cb) {
    this.lbContainerSelector = lbContainer || this.lbContainerSelector;

    this.imgData = {
      doi: imgDoi || '0'
    };
    this.imgData.strippedDoi = this.imgData.doi.replace(/^info:doi\//, '');
    this.imgList = $('.figure');

    this.insertLightboxTemplate();
    this.bindBehavior();
    $(this.lbSelector)
        .foundation('reveal', 'open');
    this.renderImg(this.imgData.doi);

    if (typeof cb === 'function') {
      cb();
    }
  };

  FigureLightbox.renderImg = function (imgDoi) {
    var $image = $(this.lbSelector).find('img.main-lightbox-image').attr('src', this.buildImgUrl(imgDoi));
    this.panZoom($image);

    // Reinitialize sliders
    $(document).foundation('slider', 'reflow');
  };


  FigureLightbox.close = function () {
    this.destroy();
    $(this.lbSelector).foundation('reveal', 'close');
  };

  FigureLightbox.showInContext = function (imgDoi) {
    imgDoi = imgDoi.split('/');
    imgDoi = imgDoi[1].slice(8);
    imgDoi = imgDoi.replace(/\./g,'-');
    return '#' + imgDoi;
  };

  FigureLightbox.panZoom = function ($image) {
    var that = this;
    this.$panZoomEl = $image.panzoom();

    /* Bind panzoom and slider to mutually control each other */
    this.bindPanZoomToSlider();
    this.bindSliderToPanZoom();

    this.$panZoomEl.parent().on('mousewheel.focal', function(e) {
      e.preventDefault();
      var delta = e.delta || e.originalEvent.wheelDelta;
      var zoomOut = delta ? delta < 0 : e.originalEvent.deltaY > 0;
      that.$panZoomEl.panzoom('zoom', zoomOut, {
        increment: 0.05,
        animate: false,
        focal: e
      });
    });
  };

  FigureLightbox.bindPanZoomToSlider = function () {
    var that = this;
    $(this.zoomRangeSelector).off('change.fndtn.slider').on('change.fndtn.slider', function(){
      var panzoomInstance = that.$panZoomEl.panzoom('instance');
      var matrix = panzoomInstance.getMatrix();
      matrix[0] = matrix[3] = this.dataset.slider;
      panzoomInstance.setMatrix(matrix);
    });
  };
  FigureLightbox.bindSliderToPanZoom = function () {
    var that = this;
    this.$panZoomEl.on('panzoomzoom', function(e, panzoom, scale) {
      $(that.zoomRangeSelector).foundation('slider', 'set_value', scale);
      // Bug in foundation unbinds after set_value. Workaround: rebind everytime
      that.bindPanZoomToSlider();
    });
  };


  FigureLightbox.buildImgUrl = function (imgDoi, options) {
    var defaultOptions = {
      path: this.imgPath,
      size: 'large'
    };
    options = _.extend(defaultOptions, options);
    return options.path + '?size=' + options.size + '&id=' + imgDoi;
  };

  FigureLightbox.destroy = function () {
    $(this.lbContainerSelector)
      // Unbind close button
        .find(this.lbCloseButtonSelector).off('click').end()
      // Unbind buttons to change images
        .find('.change-img').off('click').end()
      // Unbind button to show all images
        .find('.all-fig-btn').off('click');
  };

  })(jQuery);