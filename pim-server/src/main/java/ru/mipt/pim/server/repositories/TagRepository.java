package ru.mipt.pim.server.repositories;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Tag;
import ru.mipt.pim.server.model.User;

@Service
public class TagRepository extends CommonResourceRepository<Tag> {

	@Resource
	private FolderRepository folderRepository;
	
	public TagRepository() {
		super(Tag.class);
	}
	
	@Override
	public List<Tag> findAll(User user) {
		return (List<Tag>) CollectionUtils.union(super.findAll(user), folderRepository.findAll(user));
	}

}
