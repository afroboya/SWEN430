
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -8(%rbp)
	movq 24(%rbp), %rbx
	movq %rbx, 8(%rax)
	movq 24(%rbp), %rbx
	movq $1, %rcx
	addq %rcx, %rbx
	movq %rbx, 16(%rax)
	movq -8(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label652
label652:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq $1, %rbx
	movq %rbx, 8(%rax)
	movq $2, %rbx
	movq %rbx, 16(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $1, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label654
	movq $1, %rax
	jmp label655
label654:
	movq $0, %rax
label655:
	movq %rax, %rdi
	call assertion
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq $2, %rbx
	movq %rbx, 8(%rax)
	movq $3, %rbx
	movq %rbx, 16(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $2, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label656
	movq $1, %rax
	jmp label657
label656:
	movq $0, %rax
label657:
	movq %rax, %rdi
	call assertion
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq $9, %rbx
	movq %rbx, 8(%rax)
	movq $10, %rbx
	movq %rbx, 16(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $9, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label658
	movq $1, %rax
	jmp label659
label658:
	movq $0, %rax
label659:
	movq %rax, %rdi
	call assertion
label653:
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
