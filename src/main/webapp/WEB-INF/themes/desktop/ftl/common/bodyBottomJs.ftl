
<@js src="resource/js/global.js" />

<@js src="resource/js/pages/home.js" />
<#--The rem  polyfill rewrites the rems in to pixels. I don't think we can call this using the asset manager. -->
<!--[if IE 8]>
<script src="<@siteLink path="resource/js/vendor/rem.min.js"/>"></script>
<![endif]-->


<@renderJs />

<script type="text/javascript" src="https://www.google.com/jsapi?autoload=%7B%22modules%22%3A%5B%7B%22name%22%3A%22feeds%22%2C%22version%22%3A%221.0%22%2C%22language%22%3A%22en%22%7D%5D%7D"></script>
<script src="<@siteLink path="resource/js/components/blogfeed.js"/>"></script>
