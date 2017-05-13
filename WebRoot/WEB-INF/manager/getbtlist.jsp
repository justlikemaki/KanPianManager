<%@ page language="java"  pageEncoding="UTF-8"%>
<%@include file="taglib.jsp"%>
<link rel="stylesheet" href="css/list.css">
<link rel="stylesheet" href="css/btlist.css">
	<div class="mdl-shadow--3dp searchbt">
		<fieldset>
			<span class="likesearch">模糊</span>
			<input type="text" size="35" name="q" id="btsearch" style="width:40%;">
			<div class="searchbtn">
				<input type="submit" value="Search" id="btsearchbtn">
				<div class="loading">
					<span></span>
					<span></span>
					<span></span>
				</div>
			</div>
		</fieldset>
		<div class="selectWebsite">
			<span id="all">全选</span>
			<span id="t2" class="active">TorrentKitty</span>
			<span id="t1">onejav</span>
			<span id="t3">BtSow</span>
		    <span id="t4">Nyaa</span>
		</div>
	</div>
	<div class="context mdl-shadow--3dp">
		<ul class="contextlist" >

		</ul>	
	</div>

<div class="schedulebar"><ul class="schedulelist"></ul></div>
<div class="totop">去顶部</div>
<script type="text/javascript" src="js/touch-baidu.min.js"></script>
<script type="text/javascript" src="js/list.js"></script>