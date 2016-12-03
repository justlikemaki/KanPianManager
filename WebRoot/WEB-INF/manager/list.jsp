<%@ page language="java"  pageEncoding="UTF-8"%>
<%@include file="taglib.jsp"%>
<link rel="stylesheet" href="css/list.css">
<c:if test="${ streammode eq 1 }">
	<link rel="stylesheet" href="css/streammode.css">
</c:if>

<span id="delallinpage" class="mdl-button  mdl-button--raised mdl-button--colored">
删除本页</span>
<lf:MultiPages allcount="${pagecount}"  actionUrl="${actionUrl}" nowpage="${pagenum}" searchtype="${searchtype}"
			   searchname="${searchname}" searchvalue="${searchvalue}"  count="${countsize}"></lf:MultiPages>

<c:if test="${fn:contains(pagelist, 'empty')}">
	</br>没有数据！
</c:if>
<c:if test="${!fn:contains(pagelist, 'empty')}">
	${pagelist}
</c:if>
	<div class="context">
		<ul class="contextlist" >
		<c:forEach var="item" items="${srcs}">
			<div delid="${item.id}" class="tmpdiv singlediv contextdiv mdl-card mdl-shadow--2dp">
				<span id="${item.id}"></span><!-- 锚连接跳转位置 -->
				<div class="mdl-card__supporting-text cursor" title="${item.title}">
					<h2 class="actitle mdl-card__title-text">${item.title}</h2>
					<span>发布于:${item.times}</span>
				</div>
				<div class="mdl-card__title">
					<a href="${item.imgsrccg}" target="_blank">
						<img data-lazysrc="${item.imgsrccg}" src="img/loading.png" alt="请求图片链接失败"/>
					</a>
				</div>
				<c:if test="${item.tabtype ne 'westpron'}">
				<div class="mdl-card__actions mdl-card--border">
					<c:forEach var="tag" items="${item.tagslist}">
						<a class="searchtag" href="javascript:void(0)" title="${tag}">${tag}</a>&nbsp;&nbsp;
					</c:forEach>
				</div>
				</c:if>
				<div class="mdl-card__menu">
					<button class="ismenu mdl-button mdl-button--icon ">
						<i class="material-icons">menu</i>
					</button>
					<div class="mdl-menu-list mdl-menu__container is-upgraded " style="right: 0px; top: 32px; width: 100px;">
						<ul class="mdl-menu-ul mdl-menu--bottom-right" for="demo-menu-lower-right" data-upgraded=",MaterialMenu,MaterialRipple" >

						<li id="${item.id}"  sbm="${item.tabtype eq 'westpron' ? item.sbmsb : item.sbm}" class="onekydownmb onekydown  mdl-menu__item " tabindex="-1" data-upgraded=",MaterialRipple" style="transition-delay: 0.00s;">一键下载<span class="mdl-menu__item-ripple-container"><span class="mdl-ripple"></span></span></li>
						<li id="${item.id}"  sbm="${item.tabtype eq 'westpron' ? item.sbmsb : item.sbm}" iszip="1" class="onekydown mdl-menu__item " tabindex="-1" data-upgraded=",MaterialRipple" style="transition-delay: 0.00923076923076923s;">打包下载<span class="mdl-menu__item-ripple-container"><span class="mdl-ripple"></span></span></li>

						<c:if test="${item.tabtype ne 'westpron' && item.tabtype ne 'classical' }">
						<li class="regetbtbtn mdl-menu__item " tabindex="-1" data-upgraded=",MaterialRipple" style="transition-delay: 0.06923076923076923s;">重获BT<span class="mdl-menu__item-ripple-container"><span class="mdl-ripple"></span></span></li>
						</c:if>
						<li id="${item.id}" class="editbtn mdl-menu__item " tabindex="-1" data-upgraded=",MaterialRipple" style="transition-delay: 0.1246153846153846s;">编辑<span class="mdl-menu__item-ripple-container"><span class="mdl-ripple"></span></span></li>
						<li id="${item.id}" class="delbtn mdl-menu__item " tabindex="-1" data-upgraded=",MaterialRipple" style="transition-delay: 0.18923076923076923s;">删除<span class="mdl-menu__item-ripple-container"><span class="mdl-ripple"></span></span></li>
					</ul></div>
				</div>
				<div class="mdl-card__bottom">
					<c:if test="${item.isdown eq '0'}"><i class="material-icons" title="未存档">&#xE835;</i></c:if>
					<c:if test="${item.isdown eq '1'}"><i class="material-icons" title="硬盘存档">save</i></c:if>
					<c:if test="${item.isdown eq '2'}"><i class="material-icons" title="网络存档">web</i></c:if>
					<c:if test="${searchname eq 'sp'}">
					<i class="material-icons findthis" id="${item.id}" count="${countsize}" type="${item.tabtype}" >&#xE8B4;</i>
					</c:if>
					<a sbm="${item.sbm}" mgid="${item.id}" class="isdownload mdl-button mdl-button--colored" data-upgraded=",MaterialButton,MaterialRipple">
						download
						<span class="mdl-button__ripple-container"><span class="mdl-ripple " style="width: 241.336713648366px; height: 241.336713648366px; transform: translate(-50%, -50%) translate(57px, 19px);"></span></span>
					</a>
				</div>
				<div id="isbtlist" class="mdl-card__actions mdl-card--border">
					<ol><c:if test="${fn:length(item.btfilelistcg) >0}">
						<c:forEach var="i" begin="0" end="${fn:length(item.btfilelistcg)-1}">
								<li><a target="_blank" href="${item.btfilelistcg[i]}">${item.btnamelist[i]}</a></li>
						</c:forEach>
					</c:if></ol>
				</div>
			</div>
		</c:forEach>	
		</ul>	
	</div>
<c:if test="${!fn:contains(pagelist, 'empty')}">
	${pagelist}
</c:if>
<div class="schedulebar"><ul class="schedulelist"></ul></div>
<div class="totop">去顶部</div>
<script type="text/javascript" src="js/touch-baidu.min.js"></script>
<script type="text/javascript" src="js/list.js"></script>
<c:if test="${ streammode eq 1 }">
	<script type="text/javascript" src="js/streammode.js"></script>
</c:if>