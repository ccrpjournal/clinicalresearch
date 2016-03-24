<script type="text/template" id="metricsTileTemplate">
    <div id="<%=  name %>OnArticleMetricsTab" class="metrics_tile">
        <a href="<%=  url %>"><img id="<%=  name %>ImageOnArticleMetricsTab" src="<%=  imgSrc %>" alt="<%=  linkText %> <%=  name %>" class="metrics_tile_image"/></a>
        <div class="metrics_tile_footer" onclick="location.href=<%=  url %>">
            <a href="<%=  url %>"><%=  linkText %></a></div></div>
</script>

<script type="text/template" id="metricsTileTemplateNoLink">
    <div id="<%= name %>OnArticleMetricsTab" class="metrics_tile_no_link">
        <img id="<%= name %>ImageOnArticleMetricsTab" src="<%= imgSrc %>" alt="<%= linkText %> <%= name %>" class="metrics_tile_image"/>
        <div class="metrics_tile_footer_no_link"><%= linkText %></div></div>
</script>

<script type="text/template" id="metricsTileFacebookTooltipTemplate">
    <div class="tileTooltipContainer">
        <table class="tile_mini tileTooltip" data-js-tooltip-hover="target">
            <thead>
            <tr>
                <th>Likes</th>
                <th>Shares</th>
                <th>Posts</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="data1"> <%= likes.format(0, '.', ',') %> </td>
                <td class="data2"> <%= shares.format(0, '.', ',') %> </td>
                <td class="data1"> <%= comments.format(0, '.', ',') %> </td>
            </tr>
            </tbody>
        </table>
    </div>
</script>

<script type="text/template" id="metricsTileMendeleyTooltipTemplate">
    <div class="tileTooltipContainer" >
        <table class="tile_mini tileTooltip" data-js-tooltip-hover="target">
            <thead>
            <tr>
                <th>Individuals</th>
                <th>Groups</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="data1"> <%= individuals.format(0, '.', ',') %> </td>
                <td class="data2"> <%= groups.format(0, '.', ',') %> </td>
            </tr>
            </tbody>
        </table>
    </div>
</script>

<script type="text/template" id="pageViewsSummary">
    <div id="pageViewsSummary">
        <div id="left">
            <div class="header">Total Article Views</div>
            <div class="totalCount"> <%= total %></div>
            <div class="pubDates"> <%= pubDatesFrom %> (publication date)
                <br>through <%= pubDatesTo %> *
            </div>
        </div>
        <div id="right">
            <table id="pageViewsTable">
                <tbody>
                <tr>
                    <th></th>
                    <th nowrap="">HTML Page Views</th>
                    <th nowrap="">PDF Downloads</th>
                    <th nowrap="">XML Downloads</th>
                    <th>Totals</th>
                </tr>
                <tr>
                    <td class="source1">PLOS</td>
                    <td> <%= totalCounterHTML %></td>
                    <td> <%= totalCounterPDF %></td>
                    <td> <%= totalCounterXML %></td>
                    <td class="total"> <%= totalCouterTotal %></td>
                </tr>
                <tr>
                    <td class="source2">PMC</td>
                    <td> <%= totalPMCHTML %></td>
                    <td> <%= totalPMCPDF %></td>
                    <td>n.a.</td>
                    <td class="total"> <%= totalPMCTotal %></td>
                </tr>
                <tr>
                    <td>Totals</td>
                    <td class="total"> <%= totalHTML %></td>
                    <td  class="total"> <%= totalPDF %>
                    </td>
                    <td class="total"> <%= totalXML %>
                    </td>
                    <td class="total"> <%= total %></td>
                </tr>
                <tr class="percent">
                    <td colspan="5"><b> <%= totalViewsPDFDownloads %>
                        %</b> of article views led to PDF downloads
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

</script>