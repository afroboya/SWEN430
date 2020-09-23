
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq -16(%rbp), %rax
	jmp label438
	movq $1, %rax
	jmp label439
label438:
	movq $0, %rax
label439:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	jmp label440
	movq $1, %rax
	jmp label441
label440:
	movq $0, %rax
label441:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq %rax, -16(%rbp)
	movq -16(%rbp), %rax
	jmp label442
	movq $1, %rax
	jmp label443
label442:
	movq $0, %rax
label443:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	jmp label444
	movq $1, %rax
	jmp label445
label444:
	movq $0, %rax
label445:
	movq %rax, %rdi
	call assertion
label437:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
