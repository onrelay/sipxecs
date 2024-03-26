<!-- This goes in the document HEAD so IE7 and IE8 don't cry -->
        <!--[if lt IE 9]>
        <style type="text/css">
                table.gradienttable th {
                        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#d5e3e4', endColorstr='#b3c8cc',GradientType=0 );
                        position: relative;
                        z-index: -1;
                }
                table.gradienttable td {
                        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ebecda', endColorstr='#ceceb7',GradientType=0 );
                        position: relative;
                        z-index: -1;
                }
        </style>
        <![endif]-->

<!-- CSS goes in the document HEAD or added to your external stylesheet -->
<style type="text/css">
table.gradienttable {
        font-family: verdana,arial,sans-serif;
        font-size:11px;
        color:#333333;
        border-width: 1px;
        border-color: #999999;
        border-collapse: collapse;
}
table.gradienttable td {
background: #7d7e7d; /* Old browsers */
background: -moz-linear-gradient(top, #7d7e7d 0%, #0e0e0e 100%); /* FF3.6+ */
background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#7d7e7d), color-stop(100%,#0e0e0e)); /* Chrome,Safari4+ */
background: -webkit-linear-gradient(top, #7d7e7d 0%,#0e0e0e 100%); /* Chrome10+,Safari5.1+ */
background: -o-linear-gradient(top, #7d7e7d 0%,#0e0e0e 100%); /* Opera 11.10+ */
background: -ms-linear-gradient(top, #7d7e7d 0%,#0e0e0e 100%); /* IE10+ */
background: linear-gradient(to bottom, #7d7e7d 0%,#0e0e0e 100%); /* W3C */
filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#7d7e7d', endColorstr='#0e0e0e',GradientType=0 ); /* IE6-9 */
        border: 1px solid #999999;
}
table.gradienttable td.red {
background: #ff3019; /* Old browsers */
background: -moz-linear-gradient(top, #ff3019 12%, #cf0404 100%); /* FF3.6+ */
background: -webkit-gradient(linear, left top, left bottom, color-stop(12%,#ff3019), color-stop(100%,#cf0404)); /* Chrome,Safari4+ */
background: -webkit-linear-gradient(top, #ff3019 12%,#cf0404 100%); /* Chrome10+,Safari5.1+ */
background: -o-linear-gradient(top, #ff3019 12%,#cf0404 100%); /* Opera 11.10+ */
background: -ms-linear-gradient(top, #ff3019 12%,#cf0404 100%); /* IE10+ */
background: linear-gradient(to bottom, #ff3019 12%,#cf0404 100%); /* W3C */
filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ff3019', endColorstr='#cf0404',GradientType=0 ); /* IE6-9 */
        border: 1px solid #999999;
}


</style>

<!-- TIP: Generate your own CSS Gradients using this tool: http://www.colorzilla.com/gradient-editor/ -->
<?php
 $offset = $_REQUEST['offset'];
        $pgnum = $_REQUEST['pgnum'];
?>
<script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>
<script language="javascript" type="text/javascript">
$('#test').html('');
setInterval(function(){
      $('#test').load('index4.php?offset=<?php echo $offset;?>&pgnum=<?php echo $pgnum;?>');
 },1000);
</script>

<html>
    <body style="background:darkgray">
<?php

?>
<div id=test>
<script language="javascript" type="text/javascript">
$('#test').html('');
      $('#test').load('index4.php?offset=<?php echo $offset;?>&pgnum=<?php echo $pgnum;?>');
</script>

</div>
    </body>
</html>

