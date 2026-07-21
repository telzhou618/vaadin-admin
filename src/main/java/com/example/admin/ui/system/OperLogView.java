package com.example.admin.ui.system;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.security.RequiresPerm;
import com.example.admin.system.entity.SysOperLog;
import com.example.admin.system.service.SysOperLogService;
import com.example.admin.ui.MainLayout;
import com.example.admin.ui.PaginationBar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "system/log", layout = MainLayout.class)
@PageTitle("操作日志 - vaadin-admin")
@RequiresPerm("sys:log")
public class OperLogView extends VerticalLayout {

    private final SysOperLogService operLogService;
    private final Grid<SysOperLog> grid = new Grid<>(SysOperLog.class, false);
    private final TextField keyword = new TextField();
    private final PaginationBar paginationBar = new PaginationBar(this::loadPage);

    public OperLogView(SysOperLogService operLogService) {
        this.operLogService = operLogService;
        setSizeFull();

        H2 title = new H2("操作日志");
        title.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-xl)");

        keyword.setPlaceholder("操作人 / 操作描述");
        keyword.setClearButtonVisible(true);
        keyword.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        keyword.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        keyword.addKeyPressListener(Key.ENTER, e -> paginationBar.reset());
        Button search = new Button("搜索", e -> paginationBar.reset());
        search.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout toolbar = new HorizontalLayout(title, keyword, search);
        toolbar.setWidthFull();
        toolbar.expand(title);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        grid.addColumn(SysOperLog::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(SysOperLog::getUsername).setHeader("操作人");
        grid.addColumn(SysOperLog::getOperation).setHeader("操作");
        grid.addComponentColumn(l -> statusBadge(l.getStatus())).setHeader("状态").setWidth("90px").setFlexGrow(0);
        grid.addColumn(SysOperLog::getIp).setHeader("IP");
        grid.addColumn(l -> l.getCostMs() == null ? "" : l.getCostMs() + " ms").setHeader("耗时")
                .setWidth("90px").setFlexGrow(0);
        grid.addColumn(SysOperLog::getErrorMsg).setHeader("错误信息");
        grid.addColumn(l -> DateUtil.format(l.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).setHeader("操作时间");
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        add(toolbar, grid, paginationBar);
        refresh();
    }

    /** 状态徽标：绿色成功 / 红色失败 */
    private Component statusBadge(Integer status) {
        boolean success = Integer.valueOf(0).equals(status);
        Span badge = new Span(success ? "成功" : "失败");
        badge.getElement().getThemeList().add(success ? "badge success" : "badge error");
        return badge;
    }

    private void refresh() {
        paginationBar.refresh();
    }

    private void loadPage(int page, int pageSize) {
        Page<SysOperLog> result = operLogService.pageLogs(keyword.getValue(), page, pageSize);
        grid.setItems(result.getRecords());
        paginationBar.setTotal(result.getTotal());
    }
}
