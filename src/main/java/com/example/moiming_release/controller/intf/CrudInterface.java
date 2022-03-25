package com.example.moiming_release.controller.intf;


import com.example.moiming_release.model.network.TransferModel;

public interface CrudInterface<Req, Res> {

    TransferModel<Res> create(TransferModel<Req> request);

    TransferModel<Res> read(String uuid);

    TransferModel<Res> update(TransferModel<Req> request);

    TransferModel delete(String uuid);

}
