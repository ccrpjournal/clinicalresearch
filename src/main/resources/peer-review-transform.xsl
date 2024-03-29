<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:plos="http://plos.org"
>
  <xsl:function name="plos:get-file-extension">
    <xsl:param name="input" as="xs:string"/>
    <xsl:sequence 
        select="if (contains($input,'.'))
                then concat('.', tokenize($input,'\.')[last()])
                else $input"/>
  </xsl:function>

  <xsl:output method="html"/>

  <xsl:template match="/">
    <h2 class="page-title">Peer Review History</h2>
    <table class="review-history">
      <tbody class="peer-review-accordion">
        <xsl:apply-templates />
      </tbody>
    </table>
    <div class="peer-review-open-letter">
      <h3 class="section-title">Open letter on the publication of peer review reports</h3>
      <p>PLOS recognizes the benefits of transparency in the peer review process. Therefore, we enable the publication of all of the content of peer review and author responses alongside final, published articles. Reviewers remain anonymous, unless they choose to reveal their names.</p>
      <p>We encourage other journals to join us in this initiative. We hope that our action inspires the community, including researchers, research funders, and research institutions, to recognize the benefits of published peer review reports for all parts of the research system.</p>
      <p>
        Learn more at
        <a href="http://asapbio.org/letter" target="_blank" title="Link opens in new window">ASAPbio</a>
        .
      </p>
    </div>
  </xsl:template>

  <xsl:template match="revision">
    <xsl:variable name="revision-row">
      <xsl:number />  
    </xsl:variable>
    <tr>
      <th class="revision">
        <xsl:choose>
          <xsl:when test="$revision-row = 1">
            <span class="letter__title">Original Submission</span>
            <span class="letter__date">
              <xsl:value-of select="/peer-review/article-received-date" />
            </span>
          </xsl:when>
          <xsl:otherwise>
            <span class="letter__title">
              Revision
              <xsl:value-of select="$revision-row - 1" />
            </span>
          </xsl:otherwise>
        </xsl:choose>
      </th>
    </tr>
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="sub-article[@specific-use = 'decision-letter']">
    <tr class="peer-review-accordion-item">
      <xsl:attribute name="data-doi"><xsl:value-of select="current()/@id" /></xsl:attribute>
      <td class="letter">
        <div class="decision-letter">
          <div>
            <time class="letter__date">
              <xsl:value-of select=".//named-content[@content-type = 'letter-date']" />
            </time>
            <div class="letter__title">
              <a class="peer-review-accordion-expander" href="#">Decision Letter</a>
              -
              <span class="letter__author">
                <span>
                  <!-- decision letter editor -->
                  <xsl:apply-templates select="front-stub/contrib-group/contrib" />
                </span>
              </span>
            </div>
            <div class="peer-review-accordion-content">
              <div class="letter__body">
                  <xsl:apply-templates select="body" />
              </div>
            </div>
          </div>
        </div>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="contrib">
    <xsl:value-of select="concat(normalize-space(./name/given-names),' ',normalize-space(./name/surname),', Editor')" />
    <xsl:if test="position() != last()">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="sub-article[@specific-use = 'acceptance-letter']">
    <tr>
      <th class="revision">
        <span class="letter__title">Formally Accepted</span>
      </th>
    </tr>
    <tr class="peer-review-accordion-item">
      <xsl:attribute name="data-doi"><xsl:value-of select="current()/@id" /></xsl:attribute>
      <td class="letter">
        <div class="acceptance-letter">
          <div>
            <time class="letter__date">
              <xsl:value-of select=".//named-content[@content-type = 'letter-date']" />
            </time>
            <div class="letter__title">
              <a class="peer-review-accordion-expander" href="#">Acceptance Letter</a>
              -
              <span class="letter__author">
                <span>
                  <!-- acceptance letter editor -->
                  <xsl:apply-templates select="front-stub/contrib-group/contrib" />
                </span>
              </span>
            </div>
            <!-- accordion container -->
            <div class="peer-review-accordion-content">
              <div class="letter__body">
                  <xsl:apply-templates select="body" />
              </div>
            </div>
          </div>
        </div>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="sub-article[@article-type = 'author-comment']">
    <tr class="peer-review-accordion-item">
      <xsl:attribute name="data-doi"><xsl:value-of select="current()/@id" /></xsl:attribute>
      <td class="letter">
        <div class="author-response">
          <time class="letter__date">
            <xsl:value-of select=".//named-content[@content-type = 'author-response-date']" />
          </time>
          <div class="letter__title">
            <a class="peer-review-accordion-expander" href="#">Author Response</a>
          </div>
          <div class="peer-review-accordion-content">
            <div class="letter__body">
                <xsl:apply-templates select="body" />
            </div>
          </div>
        </div>
      </td>
    </tr>
  </xsl:template>
  
  <xsl:template match="body">
    <xsl:apply-templates select="*[not(self::supplementary-material)]" />
    <xsl:if test="supplementary-material">
      <dl class="review-files">
        <dt class="review-files__title">Attachments</dt>
        <xsl:apply-templates select="supplementary-material" />
      </dl>
    </xsl:if>

    <xsl:variable name="review-doi-url">
      <xsl:text>https://doi.org/10.1371/journal.</xsl:text><xsl:value-of select="../@id"/>
    </xsl:variable>
    <div class="review__doi">
      <a href="{$review-doi-url}">
        <xsl:value-of select="$review-doi-url"/>
      </a>
    </div>
  </xsl:template>

  <xsl:template match="p">
    <xsl:copy>
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="supplementary-material">
      <dd class="supplementary-material">
        <a class="supplementary-material__label coloration-white-on-color" 
           href="{concat('file?id=10.1371/journal.',@id,'&amp;type=supplementary')}" 
           title="{string-join(('Download',plos:get-file-extension(normalize-space(caption/p/named-content)),'file'),' ')}"
           target="_blank">
          <xsl:value-of select="label"/>
        </a>
      <div class="supplementary-material__caption">
        <xsl:copy-of select="caption/p/text()"/>
        <i>
          <xsl:value-of select="caption/p/named-content" />
        </i>
      </div>
    </dd>
  </xsl:template>
  
  <xsl:template match="list">
    <xsl:if test="title">
      <h5><xsl:value-of select="title"/></h5>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="@list-type='bullet'">
        <ul class="bulleted">
          <xsl:apply-templates/>
        </ul>
      </xsl:when>
      <xsl:otherwise>
        <ol class="{@list-type}">
          <xsl:apply-templates/>
        </ol>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="list-item">
    <li>
      <xsl:if test="../@prefix-word">
        <xsl:value-of select="../@prefix-word"/>
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

  <xsl:template match="list-item/label">
    <span class="list-label">
      <xsl:apply-templates/>
      <xsl:text>. </xsl:text>
    </span>
  </xsl:template>

  <xsl:template match="list-item/p">
    <xsl:apply-templates/>
    <xsl:if test="following-sibling::p">
      <br/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="ext-link">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="@xlink:href" />
      </xsl:attribute>
      <xsl:value-of select="text()" />
    </a>
  </xsl:template>

  <xsl:template match="email">
    <a href="mailto:{.}">
      <xsl:apply-templates/>
    </a>
  </xsl:template>
  
  <xsl:template match="named-content" />

  <!-- ignore received date found during template processing.
       date is referenced directly in revision #0 block --> 
  <xsl:template match="article-received-date" />

  <xsl:template match="bold">
    <strong>
      <xsl:apply-templates/>
    </strong>
  </xsl:template>

  <xsl:template match="italic">
    <em>
      <xsl:apply-templates/>
    </em>
  </xsl:template>

  <xsl:template match="monospace">
    <span class="monospace">
      <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="strike">
    <span class="strike">
      <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="underline">
    <span class="underline">
      <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="disp-quote">
    <blockquote>
      <xsl:call-template name="assign-id"/>
      <xsl:apply-templates/>
    </blockquote>
  </xsl:template>

  <xsl:template name="assign-id">
    <xsl:if test="@id">
      <xsl:attribute name="id">
        <xsl:value-of select="@id"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template match="sub">
    <sub>
      <xsl:apply-templates/>
    </sub>
  </xsl:template>

  <xsl:template match="sup">
    <sup>
      <xsl:apply-templates/>
    </sup>
  </xsl:template>

  <xsl:template match="body//def-list">
    <dl>
      <xsl:for-each select="def-item">
        <dt>
          <xsl:apply-templates select="term"/>
        </dt>
        <dd>
          <xsl:apply-templates select="def"/>
        </dd>
      </xsl:for-each>
    </dl>
  </xsl:template>

  <xsl:template match="def-item//p">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="def-item//named-content">
    <span class="{@content-type}">
      <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="def-item//sup | def-item//sub | def-item//em | def-item//strong">
    <xsl:element name="{local-name()}">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="term">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="def">
    <xsl:apply-templates/>
  </xsl:template>


</xsl:stylesheet>
